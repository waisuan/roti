package api

import RotiApp
import com.google.gson.Gson
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
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))
        response = Unirest.get("/machines").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines())
        )

        response = Unirest.get("/machines?page_limit=1").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines(limit = 1))
        )

        response = Unirest.get("/machines?page_limit=1&page_offset=1").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines(limit = 1, offset = 1))
        )

        response = Unirest.get("/machines?sort_filter=serialNumber").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines(sortFilter = "serialNumber", sortOrder = "ASC"))
        )

        response = Unirest.get("/machines?sort_filter=serialNumber&sort_order=DESC").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.getAllMachines(sortFilter = "serialNumber", sortOrder = "DESC"))
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
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Record already exists")
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
        assertThat(response.status).isEqualTo(500)
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
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to locate record")
    }

    @Test
    fun `SEARCH machines`() {
        val machine1 = Machine(serialNumber = "TEST01", state = "Regalia")
        val machine2 = Machine(serialNumber = "TEST02", customer = "Noctis, Sir", district = "NotImperial")
        val machine3 = Machine(serialNumber = "TEST03", reportedBy = "Prompto", brand = "Imperial")
        MachineService.createMachine(machine1)
        MachineService.createMachine(machine2)
        MachineService.createMachine(machine3)

        var response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "test")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("TEST01", "TEST02", "TEST03")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.searchMachine("test"))
        )

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "perial")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("TEST02", "TEST03")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.searchMachine("perial"))
        )

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "noctis, sir")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("TEST02")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.searchMachine("noctis, sir"))
        )

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "something something")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("[]")
        assertThat(response.body).isEqualTo(
            JavalinJson.toJson(MachineService.searchMachine("something something"))
        )

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "TEST")
            .queryString("page_limit", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        var body = Gson().fromJson(response.body, List::class.java)
        assertThat(body.size).isEqualTo(1)
        assertThat(body.first().toString()).contains("TEST01")

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "TEST")
            .queryString("page_limit", "1")
            .queryString("page_offset", "1")
            .asString()
        assertThat(response.status).isEqualTo(200)
        body = Gson().fromJson(response.body, List::class.java)
        assertThat(body.size).isEqualTo(1)
        assertThat(body.first().toString()).contains("TEST02")

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "TEST")
            .queryString("sort_filter", "serialNumber")
            .queryString("sort_order", "DESC")
            .asString()
        assertThat(response.status).isEqualTo(200)
        body = Gson().fromJson(response.body, List::class.java)
        assertThat(body.size).isEqualTo(3)
        assertThat(body.first().toString()).contains("TEST03")
        assertThat(body.last().toString()).contains("TEST01")

        response = Unirest.get("/machines/search/{keyword}")
            .routeParam("keyword", "TEST")
            .queryString("page_limit", "1")
            .queryString("page_offset", "2")
            .queryString("sort_filter", "serialNumber")
            .queryString("sort_order", "DESC")
            .asString()
        assertThat(response.status).isEqualTo(200)
        body = Gson().fromJson(response.body, List::class.java)
        assertThat(body.size).isEqualTo(1)
        assertThat(body.first().toString()).contains("TEST01")
    }

    @Test
    fun `GET number of machines`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        var response = Unirest.get("/machines/count").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("3")

        response = Unirest.get("/machines/count")
            .queryString("keyword", "TEST02")
            .asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("1")
    }

    @Test
    fun `GET machines that are due for PPM servicing`() {
        val response = Unirest.get("/machines/due").asString()
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `GET num of machines that are due for PPM servicing`() {
        val response = Unirest.get("/machines/due/count").asString()
        assertThat(response.status).isEqualTo(200)
    }
}
