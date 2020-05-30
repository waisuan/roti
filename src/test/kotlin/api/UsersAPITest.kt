package api

import RotiApp
import exceptions.IllegalUserException
import helpers.TestDatabase
import io.javalin.Javalin
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.User
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
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Record already exists")

        response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST02\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to create new user.")
    }

    @Test
    fun `POST login`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)

        var response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.headers.get("Set-Cookie").toString()).contains("USER_TOKEN=", "javalin-cookie-store=")

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"BAD_PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Incorrect username/password detected")

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST09\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Incorrect username/password detected")

        UserService.approveUser(user.username!!, false)
        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("User has not been approved")
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
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to operate on User record")

        EnvironmentVariables().set("DEV_MODE", null)
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
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to operate on User record")

        EnvironmentVariables().set("DEV_MODE", null)
    }
}
