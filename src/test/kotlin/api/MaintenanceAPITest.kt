package api

import RotiApp
import helpers.TestDatabase
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJson
import java.time.LocalDate
import kong.unirest.JsonNode
import kong.unirest.Unirest
import models.Machine
import models.Maintenance
import models.MaintenanceContainer
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
        assertThat(response.body as String).isEqualTo("{\"history\":[],\"count\":0}")

        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", reportedBy = "Dude1"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "Dude2"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", reportedBy = "Dude3"))
        response = Unirest.get("/machines/TEST01/history").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.getMaintenanceHistory("TEST01"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01")
            ))
        )

        response = Unirest.get("/machines/TEST01/history")
            .queryString("page_limit", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01")
            ))
        )

        response = Unirest.get("/machines/TEST01/history")
            .queryString("page_limit", "1")
            .queryString("page_offset", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1, offset = 1),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01")
            ))
        )

        response = Unirest.get("/machines/TEST01/history")
            .queryString("sort_filter", "workOrderNumber")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.getMaintenanceHistory("TEST01", sortFilter = "workOrderNumber"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01")
            ))
        )

        response = Unirest.get("/machines/TEST01/history")
            .queryString("sort_filter", "workOrderNumber")
            .queryString("sort_order", "DESC")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.getMaintenanceHistory("TEST01", sortFilter = "workOrderNumber", sortOrder = "DESC"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01")
            ))
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
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Record already exists")

        response = Unirest.post("/machines/TEST00000/history")
            .header("Content-Type", "application/json")
            .body(JsonNode("{\"workOrderNumber\":\"W02\", \"workOrderDate\":\"2020-01-01\"}"))
            .asString()
        assertThat(response.status).isEqualTo(500)
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
        assertThat(response.status).isEqualTo(500)
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
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to locate record")
    }

    @Test
    fun `SEARCH history`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", reportedBy = "Dude1", workOrderDate = LocalDate.parse("2020-03-03")))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "Dude2 Dood"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", reportedBy = "Dude3"))

        var response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "w0")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("W01", "W02", "W03")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "w0"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "w0")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "dood")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("W02")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "dood"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "dood")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "03-03")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("W01")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "03-03"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "03-03")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "w0")
            .queryString("page_limit", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "w0", limit = 1),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "w0")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "w0")
            .queryString("page_limit", "1")
            .queryString("page_offset", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "w0", limit = 1, offset = 1),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "w0")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "w0")
            .queryString("sort_filter", "workOrderNumber")
            .queryString("sort_order", "ASC")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "w0", sortFilter = "workOrderNumber", sortOrder = "ASC"),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "w0")
            ))
        )

        response = Unirest.get("/machines/TEST01/history/search/{keyword}")
            .routeParam("keyword", "w0")
            .queryString("page_limit", "1")
            .queryString("page_offset", "2")
            .queryString("sort_filter", "workOrderNumber")
            .queryString("sort_order", "ASC")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MaintenanceContainer(
                history = MaintenanceService.searchMaintenanceHistory("TEST01", "w0", sortFilter = "workOrderNumber", sortOrder = "ASC", limit = 1, offset = 2),
                count = MaintenanceService.getNumberOfMaintenanceRecords("TEST01", "w0")
            ))
        )
    }

    @Test
    fun `GET number of history records`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", reportedBy = "Dude1"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "Dude2"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", reportedBy = "Dude3"))

        var response = Unirest.get("/machines/TEST01/history/count").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("3")

        response = Unirest.get("/machines/TEST01/history/count")
            .queryString("keyword", "W03")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("1")
    }
}
