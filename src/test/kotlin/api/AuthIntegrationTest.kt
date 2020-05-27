package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import services.UserService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {
    private lateinit var app: Javalin

    @BeforeAll
    fun setup() {
        app = RotiApp(port = 8000, enableDB = false).init()
        Unirest.config().defaultBaseUrl("http://localhost:8000")
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
        assertThat(response.headers.get("WWW-Authenticate")).isNotNull()

        response = Unirest.get("/machines/TEST/history").asString()
        assertThat(response.status).isEqualTo(401)
        assertThat(response.headers.get("WWW-Authenticate")).isNotNull()

        response = Unirest.get("/files/TEST").asString()
        assertThat(response.status).isEqualTo(401)
        assertThat(response.headers.get("WWW-Authenticate")).isNotNull()

        val user = User(username = "TEST", password = "PASSWORD", email = "email@mail.com")
        UserService.createUser(user)
        val token = UserService.loginUser(user)

        response = Unirest.get("/machines")
            .header("Authorization", "Bearer $token")
            .asString()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.get("/machines")
            .header("Authorization", "Bearer BAD_TOKEN")
            .asString()
        assertThat(response.status).isEqualTo(401)
        assertThat(response.headers.get("WWW-Authenticate")).isNotNull()
    }

    @Test
    fun `register and login requests do not require authentication`() {
        var response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)

        response = Unirest.post("/users/login")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
    }
}
