package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import kong.unirest.JsonNode
import kong.unirest.Unirest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsersAPITest {
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
    fun `POST register`() {
        var response = Unirest.post("/users/register")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"username\":\"TEST\", \"password\":\"PASSWORD\", \"email\":\"email@mail.com\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
    }
}
