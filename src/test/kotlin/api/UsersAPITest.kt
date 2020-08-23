package api

import RotiApp
import exceptions.IllegalUserException
import helpers.TestDatabase
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJson
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.json.JSONArray
import models.Constants
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import services.UserService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsersAPITest {
    private lateinit var app: Javalin

    @BeforeAll
    fun setup() {
        app = RotiApp(port = 8000, enableDB = false).init()
        Unirest.config().defaultBaseUrl("http://localhost:8000/api")
    }

    @AfterAll
    fun tearDown() {
        app.stop()

        Thread.sleep(5_000) // Hack to allow the web server to properly shutdown before continuing on with the test suite.
    }

    @BeforeEach
    fun beforeEachTest() {
        TestDatabase.init()
    }

    @AfterEach
    fun afterEachTest() {
        TestDatabase.purge()
    }

    @Test
    fun `POST register`() {
        var response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Record already exists")

        response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST02\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to create new user.")
    }

    @Test
    fun `POST login`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)

        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.cookies.getNamed(Constants.USER_TOKEN.name).value).isNotEmpty()
        assertThat(response.cookies.getNamed(Constants.USER_NAME.name).value).isEqualTo("TEST")
        assertThat(response.body).isEqualTo("false")

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"BAD_PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Incorrect username/password detected")

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST09\", \"password\":\"PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Incorrect username/password detected")

        UserService.approveUser(user.username!!, false)
        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("User has not been approved")
    }

    @Test
    fun `POST login returns true if user is an admin`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.updateUser(user.username!!, User(role = UserRole.ADMIN))
        UserService.approveUser(user.username!!, true)

        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.cookies.getNamed(Constants.USER_TOKEN.name).value).isNotEmpty()
        assertThat(response.cookies.getNamed(Constants.USER_NAME.name).value).isEqualTo("TEST")
        assertThat(response.body).isEqualTo("true")
    }

    @Test
    fun `POST logout`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)

        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.cookies.getNamed(Constants.USER_TOKEN.name).value).isNotEmpty()
        assertThat(response.cookies.getNamed(Constants.USER_NAME.name).value).isEqualTo("TEST")

        response = Unirest.get("/machines").asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.post("/users/logout").asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.cookies.getNamed(Constants.USER_TOKEN.name).value).isEmpty()
        assertThat(response.cookies.getNamed(Constants.USER_NAME.name).value).isEmpty()

        response = Unirest.get("/machines").asEmpty()
        assertThat(response.status).isEqualTo(401)
    }

    @Test
    fun `PUT users`() {
        EnvironmentVariables().set("DEV_MODE", "1")

        var user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        assertThat(UserService.loginUser(user)).isNotEmpty()

        var response = Unirest.put("/users/TEST")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"password\":\"NEW_PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThatThrownBy {
            UserService.loginUser(user)
        }.isInstanceOf(IllegalUserException::class.java)

        user = User(username = "TEST", password = "NEW_PASSWORD")
        assertThat(UserService.loginUser(user)).isNotEmpty()

        response = Unirest.put("/users/TEST09")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"password\":\"NEW_PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to operate on User record")

        EnvironmentVariables().set("DEV_MODE", null)
    }

    @Test
    fun `PUT users is only allowed for ADMIN users`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.put("/users/TEST")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"password\":\"NEW_PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(401)

        UserService.updateUser(user.username!!, User(role = UserRole.ADMIN))
        response = Unirest.put("/users/TEST")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"password\":\"NEW_PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `PUT multiple users`() {
        EnvironmentVariables().set("DEV_MODE", "1")

        UserService.createUser(User(username = "TEST", password = "PASSWORD", email = "email@mail.com"))
        UserService.createUser(User(username = "TEST2", password = "PASSWORD", email = "email2@mail.com"))

        var response = Unirest.put("/users")
            .header("Content-Type", "application/json")
            .body(JSONArray("""
                [{"username":"TEST","password":"NEW_PASSWORD", "is_approved":"true"},
                 {"username":"TEST2","email":"new_mail@mail.com"}]
            """.trimIndent()))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        assertThat(UserService.loginUser(User(username = "TEST", password = "NEW_PASSWORD"))).isNotNull()
        assertThat(UserService.getUser("TEST2")!!.email).isEqualTo("new_mail@mail.com")

        EnvironmentVariables().set("DEV_MODE", null)
    }

    @Test
    fun `PUT multiple users is only allowed for ADMIN users`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.put("/users")
            .header("Content-Type", "application/json")
            .body(JSONArray("""
                [{"username":"TEST","password":"NEW_PASSWORD"}]
            """.trimIndent()))
            .asEmpty()
        assertThat(response.status).isEqualTo(401)

        UserService.updateUser(user.username!!, User(role = UserRole.ADMIN))
        response = Unirest.put("/users")
            .header("Content-Type", "application/json")
            .body(JSONArray("""
                [{"username":"TEST","password":"NEW_PASSWORD"}]
            """.trimIndent()))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `DELETE users`() {
        EnvironmentVariables().set("DEV_MODE", "1")

        var user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        assertThat(UserService.loginUser(user)).isNotEmpty()

        var response = Unirest.delete("/users/TEST").asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThatThrownBy {
            UserService.loginUser(user)
        }.isInstanceOf(IllegalUserException::class.java)

        response = Unirest.delete("/users/TEST").asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to operate on User record")

        EnvironmentVariables().set("DEV_MODE", null)
    }

    @Test
    fun `DELETE users is only allowed for ADMIN users`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.delete("/users/TEST").asEmpty()
        assertThat(response.status).isEqualTo(401)

        UserService.updateUser(user.username!!, User(role = UserRole.ADMIN))
        response = Unirest.delete("/users/TEST").asEmpty()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `DELETE multiple users`() {
        EnvironmentVariables().set("DEV_MODE", "1")

        UserService.createUser(User(username = "TEST", password = "PASSWORD", email = "email@mail.com"))
        UserService.createUser(User(username = "TEST2", password = "PASSWORD", email = "email2@mail.com"))

        val response = Unirest.delete("/users")
            .queryString("users", listOf("TEST", "TEST2"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        assertThat(UserService.getUser("TEST")).isNull()
        assertThat(UserService.getUser("TEST2")).isNull()

        EnvironmentVariables().set("DEV_MODE", null)
    }

    @Test
    fun `DELETE multiple users is only allowed for ADMIN users`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.delete("/users")
            .queryString("users", listOf("TEST"))
            .asEmpty()
        assertThat(response.status).isEqualTo(401)

        UserService.updateUser(user.username!!, User(role = UserRole.ADMIN))
        response = Unirest.delete("/users")
            .queryString("users", listOf("TEST"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `GET users`() {
        EnvironmentVariables().set("DEV_MODE", "1")
        var response = Unirest.get("/users").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body as String).isEqualTo("[]")

        UserService.createUser(User(username = "TEST", password = "PASSWORD", email = "email@mail.com"))
        response = Unirest.get("/users").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body as String).isEqualTo(
            JavalinJson.toJson(
                UserService.getUsers()
            )
        )

        EnvironmentVariables().set("DEV_MODE", null)
    }

    @Test
    fun `GET users is only allowed for ADMIN users`() {
        var response = Unirest.get("/users").asString()
        assertThat(response.status).isEqualTo(401)

        UserService.createUser(User(username = "TEST", password = "PASSWORD", email = "email@mail.com"))
        UserService.approveUser("TEST", true)
        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.get("/users").asString()
        assertThat(response.status).isEqualTo(401)

        UserService.updateUser("TEST", User(role = UserRole.ADMIN))
        response = Unirest.get("/users").asString()
        assertThat(response.body as String).isEqualTo(
            JavalinJson.toJson(
                UserService.getUsers()
            )
        )
    }
}
