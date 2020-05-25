package models

import io.javalin.plugin.json.JavalinJson
import java.time.LocalDate
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MachineTest {
    @Test
    fun `tncDate should have a pattern of yyyy-MM-dd`() {
        val machine = Machine(tncDate = LocalDate.parse("2020-01-01"))
        assertThat(JavalinJson.toJson(machine)).contains("\"tncDate\":\"2020-01-01\"")
    }

    @Test
    fun `ppmDate should have a pattern of yyyy-MM-dd`() {
        val machine = Machine(ppmDate = LocalDate.parse("2020-01-01"))
        assertThat(JavalinJson.toJson(machine)).contains("\"ppmDate\":\"2020-01-01\"")
    }

    @Test
    fun `createdAt should have a pattern of yyyy-MM-dd HH mm ss`() {
        val machine = Machine(createdAt = LocalDateTime.parse("2020-01-01T10:10:10"))
        assertThat(JavalinJson.toJson(machine)).contains("\"createdAt\":\"2020-01-01 10:10:10\"")
    }

    @Test
    fun `updatedAt should have a pattern of yyyy-MM-dd HH mm ss`() {
        val machine = Machine(updatedAt = LocalDateTime.parse("2020-01-01T10:10:10"))
        assertThat(JavalinJson.toJson(machine)).contains("\"updatedAt\":\"2020-01-01 10:10:10\"")
    }
}
