package services

import exceptions.BadOperationException
import exceptions.RecordNotFoundException
import helpers.TestDatabase
import java.lang.Exception
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
    fun `createMaintenanceHistory() should create a new maintenance record successfully`() {
        var newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        val createdMaintenance = MaintenanceService.getMaintenanceHistory("TEST01").firstOrNull()
        assertThat(createdMaintenance).isNotNull
        assertThat(createdMaintenance!!.workOrderNumber).isEqualTo("W01")

        assertThatThrownBy {
            MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)
        }.isInstanceOf(Exception::class.java)
            .hasMessageContaining("duplicate key")

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
        MaintenanceService.updateMaintenanceHistory("W01", updated)

        val maintenance = MaintenanceService.getMaintenanceHistory("TEST01").firstOrNull()
        assertThat(maintenance).isNotNull
        assertThat(maintenance!!.workOrderDate).isEqualTo(LocalDate.parse("2020-01-01"))

        assertThatThrownBy {
            MaintenanceService.updateMaintenanceHistory("DUMMY", updated)
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `deleteMaintenanceHistory() should delete maintenance record successfully`() {
        val newMaintenance = Maintenance(workOrderNumber = "W01")
        MaintenanceService.createMaintenanceHistory("TEST01", newMaintenance)

        MaintenanceService.deleteMaintenanceHistory("W01")

        assertThat(MaintenanceService.getMaintenanceHistory("TEST01"))
            .isEmpty()

        assertThatThrownBy {
            MaintenanceService.deleteMaintenanceHistory("W01")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }
}
