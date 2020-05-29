package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJson
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.Machine
import models.Maintenance
import org.assertj.core.api.Assertions.assertThat
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import services.MachineService
import services.MaintenanceService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MaintenanceAPITest {
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
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
    }

    @AfterEach
    fun afterEachTest() {
        TestDatabase.purge()
    }

    @Test
    fun `GET history`() {
        var response = Unirest.get("/machines/TEST01/history").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("[]")

        val maintenance = Maintenance(workOrderNumber = "W01", reportedBy = "Dude")
        MaintenanceService.createMaintenanceHistory("TEST01", maintenance)
        response = Unirest.get("/machines/TEST01/history").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceService.getMaintenanceHistory("TEST01"))
        )
    }

    @Test
    fun `POST history`() {
        var response = Unirest.post("/machines/TEST01/history")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"workOrderNumber\":\"W01\", \"workOrderDate\":\"2020-01-01\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(MaintenanceService.getMaintenanceHistory("TEST01").size).isEqualTo(1)

        response = Unirest.post("/machines/TEST01/history")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"workOrderNumber\":\"W01\", \"workOrderDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("duplicate key value violates unique constraint")

        response = Unirest.post("/machines/TEST00000/history")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"workOrderNumber\":\"W02\", \"workOrderDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Key (serialNumber)=(TEST00000) is not present in table \"machines\"")
    }

    @Test
    fun `PUT history`() {
        val maintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", maintenance)

        var response = Unirest.put("/machines/TEST01/history/W01")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"actionTaken\":\"Something\", \"workOrderDate\":\"2011-06-12\"}"))
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(MaintenanceService.getMaintenanceHistory("TEST01").first().actionTaken)
            .isEqualTo("Something")
        assertThat(MaintenanceService.getMaintenanceHistory("TEST01").first().workOrderDate)
            .isEqualTo("2011-06-12")

        response = Unirest.put("/machines/TEST01/history/W00000")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"actionTaken\":\"Something\", \"workOrderDate\":\"2011-06-12\"}"))
            .asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to locate record")
    }

    @Test
    fun `DELETE history`() {
        val maintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", maintenance)

        var response = Unirest.delete("/machines/TEST01/history/W01").asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(MaintenanceService.getMaintenanceHistory("TEST01").firstOrNull())
            .isNull()

        response = Unirest.delete("/machines/TEST01/history/W00000").asString()
        assertThat(response.status).isEqualTo(404)
        assertThat(response.body as String).contains("Unable to locate record")
    }
}
