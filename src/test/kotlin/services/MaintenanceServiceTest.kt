package services

import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import helpers.TestDatabase
import java.time.LocalDate
import models.Machine
import models.Maintenance
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MaintenanceServiceTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
    }

    @AfterEach
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `getMaintenanceHistory() should return maintenance records accordingly`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        assertThat(MaintenanceService.getMaintenanceHistory("TEST01"))
            .isNotEmpty
        assertThat(MaintenanceService.getMaintenanceHistory("TEST01").size)
            .isEqualTo(1)
        assertThat(MaintenanceService.getMaintenanceHistory("TEST02"))
            .isEmpty()
    }

    @Test
    fun `getMaintenanceHistory() with limit & offset should return maintenance records successfully`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03"))

        var found = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1, offset = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1, offset = 2)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W03")

        found = MaintenanceService.getMaintenanceHistory("TEST01", limit = 1, offset = 3)
        assertThat(found).isEmpty()
    }

    @Test
    fun `getMaintenanceHistory() should return machine records in a specified order`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", reportedBy = "C"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "A"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", reportedBy = "B"))

        var found = MaintenanceService.getMaintenanceHistory("TEST01", sortFilter = "reportedBy")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W02")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.getMaintenanceHistory("TEST01", sortFilter = "reportedBy", sortOrder = "DESC")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W01")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W02")
    }

    @Test
    fun `createMaintenanceHistory() should create a new maintenance record successfully`() {
        var newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        val createdMaintenance = MaintenanceService.getMaintenanceHistory("TEST01").firstOrNull()
        assertThat(createdMaintenance).isNotNull
        assertThat(createdMaintenance!!.workOrderNumber).isEqualTo("W01")

        assertThatThrownBy {
            MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)
        }.isInstanceOf(RecordAlreadyExistsException::class.java)

        newMaintenance = Maintenance()
        assertThatThrownBy {
            MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on Maintenance record")
    }

    @Test
    fun `updateMaintenanceHistory() should update maintenance record successfully`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        val updated = Maintenance(workOrderDate = LocalDate.parse("2020-01-01"))
        MaintenanceService.updateMaintenanceHistory("TEST01", "W01", updated)

        val maintenance = MaintenanceService.getMaintenanceHistory("TEST01").firstOrNull()
        assertThat(maintenance).isNotNull
        assertThat(maintenance!!.workOrderDate).isEqualTo(LocalDate.parse("2020-01-01"))

        assertThatThrownBy {
            MaintenanceService.updateMaintenanceHistory("TEST01", "DUMMY", updated)
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `deleteMaintenanceHistory() should delete maintenance record successfully`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        MaintenanceService.deleteMaintenanceHistory("TEST01", "W01")

        assertThat(MaintenanceService.getMaintenanceHistory("TEST01"))
            .isEmpty()

        assertThatThrownBy {
            MaintenanceService.deleteMaintenanceHistory("TEST01", "W01")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `searchMaintenanceHistory() should return search results as expected`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", workOrderType = "test TEST"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "someone"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", workOrderType = "some_type"))

        var found = MaintenanceService.searchMaintenanceHistory("TEST01", "someone")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(listOf("W02"))

        found = MaintenanceService.searchMaintenanceHistory("TEST01", "some")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(listOf("W02", "W03"))

        found = MaintenanceService.searchMaintenanceHistory("TEST02", "someone")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(emptyList<String>())
    }

    @Test
    fun `searchMaintenanceHistory() with limit & offset should return search results as expected`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", workOrderType = "test TEST"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", reportedBy = "someone"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", workOrderType = "some_type"))

        var found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", limit = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", limit = 1, offset = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", limit = 1, offset = 2)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W03")

        found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", limit = 1, offset = 3)
        assertThat(found).isEmpty()
    }

    @Test
    fun `searchMaintenanceHistory() should return search results in a specified order`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01", workOrderType = "C"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02", workOrderType = "A"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03", workOrderType = "B"))

        var found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", sortFilter = "workOrderType")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W02")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.searchMaintenanceHistory("TEST01", "W0", sortFilter = "workOrderType", sortOrder = "DESC")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W01")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W02")
    }

    @Test
    fun `getNumberOfMaintenanceRecords() returns number of maintenance history records in the DB`() {
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W01"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W02"))
        MaintenanceService.createMaintenanceHistory("TEST01", Maintenance(workOrderNumber = "W03"))

        assertThat(MaintenanceService.getNumberOfMaintenanceRecords("TEST01")).isEqualTo(3)
        assertThat(MaintenanceService.getNumberOfMaintenanceRecords("TEST01", keyword = "W02")).isEqualTo(1)
        assertThat(MaintenanceService.getNumberOfMaintenanceRecords("UNKNOWN")).isEqualTo(0)
    }
}
