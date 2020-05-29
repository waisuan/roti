package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJson
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import services.MachineService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MachinesAPITest {
    private lateinit var app: Javalin

    @BeforeAll
    fun setup() {
        EnvironmentVariables().set("DEV_MODE", "1")

        app = RotiApp(port = 8000, enableDB = false).init()
        Unirest.config().defaultBaseUrl("http://localhost:8000/api")
    }

    @AfterAll
    fun tearDown() {
        app.stop()

        EnvironmentVariables().set("DEV_MODE", null)
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
    fun `GET machines`() {
        var response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body as String).isEqualTo("[]")

        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines())
        )
    }

    @Test
    fun `POST machines`() {
        var response = Unirest.post("/machines")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"serialNumber\":\"TEST02\", \"tncDate\":\"2020-01-01\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(MachineService.getAllMachines().size).isEqualTo(1)

        response = Unirest.post("/machines")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"serialNumber\":\"TEST02\", \"tncDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("duplicate key value violates unique constraint")
    }

    @Test
    fun `PUT machines`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        var response = Unirest.put("/machines/TEST01")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"ppmDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(MachineService.getAllMachines().first().ppmDate)
            .isEqualTo("2020-01-01")

        response = Unirest.put("/machines/TEST_")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"ppmDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to locate record")
    }

    @Test
    fun `DELETE machines`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        var response = Unirest.delete("/machines/TEST01").asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(MachineService.getAllMachines().firstOrNull())
            .isNull()

        response = Unirest.delete("/machines/TEST01").asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to locate record")
    }
}
