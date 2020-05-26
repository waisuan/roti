package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import kong.unirest.Unirest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

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
    }

    @BeforeEach
    fun beforeEachTest() {
        TestDatabase.init()
    }

    @AfterEach
    fun afterEachTest() {
        TestDatabase.purge()
    }

    // @Test
    // fun ``() {
    //
    // }
}
