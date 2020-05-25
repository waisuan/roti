package models

import io.javalin.plugin.json.JavalinJson
import java.time.LocalDate
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MaintenanceTest {
    @Test
    fun `workOrderDate should have a pattern of yyyy-MM-dd`() {
        val maintenance = Maintenance(workOrderDate = LocalDate.parse("2020-01-01"))
        assertThat(JavalinJson.toJson(maintenance)).contains("\"workOrderDate\":\"2020-01-01\"")
    }

    @Test
    fun `createdAt should have a pattern of yyyy-MM-dd HH mm ss`() {
        val maintenance = Maintenance(createdAt = LocalDateTime.parse("2020-01-01T10:10:10"))
        assertThat(JavalinJson.toJson(maintenance)).contains("\"createdAt\":\"2020-01-01 10:10:10\"")
    }

    @Test
    fun `updatedAt should have a pattern of yyyy-MM-dd HH mm ss`() {
        val maintenance = Maintenance(updatedAt = LocalDateTime.parse("2020-01-01T10:10:10"))
        assertThat(JavalinJson.toJson(maintenance)).contains("\"updatedAt\":\"2020-01-01 10:10:10\"")
    }
}
