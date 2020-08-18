package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import java.time.LocalDate
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.Constants
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import services.UserService
import utils.Validator
import utils.logger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {
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
    fun `API requests require authentication`() {
        var response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(401)

        response = Unirest.get("/machines/TEST/history").asString()
        assertThat(response.status).isEqualTo(401)

        response = Unirest.get("/files/TEST").asString()
        assertThat(response.status).isEqualTo(401)

        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        val token = UserService.loginUser(user)

        response = Unirest.get("/machines")
            .cookie(Constants.USER_TOKEN.name, token)
            .cookie(Constants.USER_NAME.name, user.username)
            .asString()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.get("/machines")
            .cookie(Constants.USER_TOKEN.name, "BAD_TOKEN")
            .cookie(Constants.USER_NAME.name, user.username)
            .asString()
        assertThat(response.status).isEqualTo(401)

        response = Unirest.get("/machines")
            .cookie(Constants.USER_TOKEN.name, token)
            .cookie(Constants.USER_NAME.name, "SOME_USER")
            .asString()
        assertThat(response.status).isEqualTo(401)
    }

    @Test
    fun `register and login requests do not require authentication`() {
        var response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        UserService.approveUser("TEST", true)

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `cookies are init after user is logged in`() {
        var response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(401)

        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.cookies.getNamed(Constants.USER_TOKEN.name).value).isNotEmpty()
        assertThat(response.cookies.getNamed(Constants.USER_NAME.name).value).isNotEmpty()

        response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `only permitted roles are allowed access if they're declared`() {
        var response = Unirest.get("http://localhost:8000/admin").asString()
        assertThat(response.status).isEqualTo(401)

        var user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.get("http://localhost:8000/admin").asString()
        assertThat(response.status).isEqualTo(401)

        user = User(role = UserRole.ADMIN)
        UserService.updateUser("TEST", user)
        response = Unirest.get("http://localhost:8000/admin").asString()
        assertThat(response.status).isEqualTo(200)

        user = User(role = UserRole.GUEST)
        UserService.updateUser("TEST", user)
        response = Unirest.get("http://localhost:8000/api/machines").asString()
        assertThat(response.status).isEqualTo(401)

        user = User(role = UserRole.NON_ADMIN)
        UserService.updateUser("TEST", user)
        response = Unirest.get("http://localhost:8000/api/machines").asString()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `JWT should be renewed if they are almost expired`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        val token = Validator.generateToken(expiresAt = LocalDate.now().plusDays(1))

        val response = Unirest.get("/machines")
            .cookie(Constants.USER_TOKEN.name, token)
            .cookie(Constants.USER_NAME.name, user.username)
            .asString()
        assertThat(response.status).isEqualTo(200)
        val refreshedCookie = response.cookies.getNamed(Constants.USER_TOKEN.name).value
        assertThat(refreshedCookie).isNotEmpty()
        assertThat(refreshedCookie).isNotEqualTo(token)
    }

    @Test
    fun `JWT should not be renewed if LOGOUT endpoint was called`() {
        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        val token = Validator.generateToken(expiresAt = LocalDate.now().plusDays(1))

        val response = Unirest.post("/users/logout")
            .cookie(Constants.USER_TOKEN.name, token)
            .cookie(Constants.USER_NAME.name, user.username)
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        val refreshedCookie = response.cookies.getNamed(Constants.USER_TOKEN.name).value
        assertThat(refreshedCookie).isNullOrEmpty()
    }
}
