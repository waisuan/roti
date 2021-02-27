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
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MaintenanceServiceTest {
    private val machineUnderTestSerialNumber = "TEST01"

    @BeforeEach
    fun setup() {
        TestDatabase.init()
        MachineService.createMachine(Machine(serialNumber = machineUnderTestSerialNumber))
    }

    @AfterEach
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `getMaintenanceHistory() should return maintenance records accordingly`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)

        assertThat(MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber))
            .isNotEmpty
        assertThat(MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).size)
            .isEqualTo(1)
        assertThat(MaintenanceService.getMaintenanceHistory("TEST02"))
            .isEmpty()
    }

    @Test
    fun `getMaintenanceHistory() with limit & offset should return maintenance records successfully`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03"))

        var found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, limit = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W03")

        found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, limit = 1, offset = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, limit = 1, offset = 2)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, limit = 1, offset = 3)
        assertThat(found).isEmpty()
    }

    @Test
    fun `getMaintenanceHistory() should return machine records in a specified order`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", reportedBy = "C"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02", reportedBy = "A"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03", reportedBy = "B"))

        var found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, sortFilter = "reportedBy")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W01")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber, sortFilter = "reportedBy", sortOrder = "ASC")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W02")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W01")
    }

    @Test
    fun `createMaintenanceHistory() should create a new maintenance record successfully`() {
        var newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)

        val createdMaintenance = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).firstOrNull()
        assertThat(createdMaintenance).isNotNull
        assertThat(createdMaintenance!!.workOrderNumber).isEqualTo("W01")

        assertThatThrownBy {
            MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)
        }.isInstanceOf(RecordAlreadyExistsException::class.java)

        newMaintenance = Maintenance()
        assertThatThrownBy {
            MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on Maintenance record")
    }

    @Test
    fun `updateMaintenanceHistory() should update maintenance record successfully`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)

        val updated = Maintenance(workOrderDate = LocalDate.parse("2020-01-01"))
        MaintenanceService.updateMaintenanceHistory(machineUnderTestSerialNumber, "W01", updated)

        val maintenance = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).firstOrNull()
        assertThat(maintenance).isNotNull
        assertThat(maintenance!!.workOrderDate).isEqualTo(LocalDate.parse("2020-01-01"))

        assertThatThrownBy {
            MaintenanceService.updateMaintenanceHistory(machineUnderTestSerialNumber, "DUMMY", updated)
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `deleteMaintenanceHistory() should delete maintenance record successfully`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, newMaintenance)

        MaintenanceService.deleteMaintenanceHistory(machineUnderTestSerialNumber, "W01")

        assertThat(MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber))
            .isEmpty()

        assertThatThrownBy {
            MaintenanceService.deleteMaintenanceHistory(machineUnderTestSerialNumber, "W01")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `searchMaintenanceHistory() should return search results as expected`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "test TEST"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02", reportedBy = "someone"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03", workOrderType = "some_type"))

        var found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "someone")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(listOf("W02"))

        found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "some")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(listOf("W03", "W02"))

        found = MaintenanceService.searchMaintenanceHistory("TEST02", "someone")
        assertThat(found.map { it.workOrderNumber }).isEqualTo(emptyList<String>())
    }

    @Test
    fun `searchMaintenanceHistory() with limit & offset should return search results as expected`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "test TEST"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02", reportedBy = "someone"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03", workOrderType = "some_type"))

        var found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", limit = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W03")

        found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", limit = 1, offset = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", limit = 1, offset = 2)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().workOrderNumber).isEqualTo("W01")

        found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", limit = 1, offset = 3)
        assertThat(found).isEmpty()
    }

    @Test
    fun `searchMaintenanceHistory() should return search results in a specified order`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02", workOrderType = "A"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03", workOrderType = "B"))

        var found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", sortFilter = "workOrderType")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W01")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W02")

        found = MaintenanceService.searchMaintenanceHistory(machineUnderTestSerialNumber, "W0", sortFilter = "workOrderType", sortOrder = "ASC")
        assertThat(found).isNotEmpty
        assertThat(found.first().workOrderNumber).isEqualTo("W02")
        assertThat(found[1].workOrderNumber).isEqualTo("W03")
        assertThat(found.last().workOrderNumber).isEqualTo("W01")
    }

    @Test
    fun `getNumberOfMaintenanceRecords() returns number of maintenance history records in the DB`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W02"))
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W03"))

        assertThat(MaintenanceService.getNumberOfMaintenanceRecords(machineUnderTestSerialNumber)).isEqualTo(3)
        assertThat(MaintenanceService.getNumberOfMaintenanceRecords(machineUnderTestSerialNumber, keyword = "W02")).isEqualTo(1)
        assertThat(MaintenanceService.getNumberOfMaintenanceRecords("UNKNOWN")).isEqualTo(0)
    }

    @Test
    fun `Concurrent updates should not override one another`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))

        val history = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber)
        val history1 = history.first()
        val history2 = history.first()

        MaintenanceService.updateMaintenanceHistory(history1.serialNumber!!, history1.workOrderNumber!!, history1)
        assertThatThrownBy { MaintenanceService.updateMaintenanceHistory(history2.serialNumber!!, history2.workOrderNumber!!, history2) }
            .isInstanceOf(ExposedSQLException::class.java)
            .hasMessageContaining("Updated record looks outdated.")
    }

    @Test
    fun `createMaintenanceHistory() should stamp createdAt and updatedAt fields`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))

        MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.createdAt).isNotNull
            assertThat(it.updatedAt).isNotNull
            assertThat(it.createdAt).isEqualTo(it.updatedAt)
        }
    }

    @Test
    fun `updateMaintenanceHistory() should refresh updatedAt field`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))
        val history = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.createdAt).isEqualTo(it.updatedAt)
            it
        }

        MaintenanceService.updateMaintenanceHistory(history.serialNumber!!, history.workOrderNumber!!, history.copy(reportedBy = "SOME DUDE"))
        MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.updatedAt).isNotNull
            assertThat(it.createdAt).isNotEqualTo(it.updatedAt)
        }
    }

    @Test
    fun `Version column for new maintenance records start at 1`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))

        MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.version).isEqualTo(1)
        }
    }

    @Test
    fun `Version column on maintenance records gets incremented at every update`() {
        MaintenanceService.createMaintenanceHistory(machineUnderTestSerialNumber, Maintenance(workOrderNumber = "W01", workOrderType = "C"))

        val history = MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.version).isEqualTo(1)
            it
        }

        MaintenanceService.updateMaintenanceHistory(history.serialNumber!!, history.workOrderNumber!!, history.copy(reportedBy = "SOME DUDE"))
        MaintenanceService.getMaintenanceHistory(machineUnderTestSerialNumber).first().let {
            assertThat(it.version).isEqualTo(2)
        }
    }
}
