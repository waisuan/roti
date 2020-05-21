package services

import exceptions.RecordNotFoundException
import helpers.TestDatabase
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test

class MachineServiceTest {
    @Before
    fun setup() {
        TestDatabase.init()
    }

    @After
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `createMachine() should create a new machine record successfully`() {
        val newMachine = Machine(serialNumber = "TEST01")
        MachineService.createMachine(newMachine)

        val createdMachine = MachineService.getAllMachines().firstOrNull()
        assertThat(createdMachine).isNotNull
        assertThat(createdMachine!!.serialNumber).isEqualTo("TEST01")
    }

    @Test
    fun `updateMachine() should update machine record successfully`() {
        val newMachine = Machine(serialNumber = "TEST01")
        MachineService.createMachine(newMachine)

        val updatedMachine = Machine(serialNumber = "TEST01", reportedBy = "DR.CODE")
        MachineService.updateMachine(serialNumber = "TEST01", updatedMachine = updatedMachine)

        val createdMachine = MachineService.getAllMachines().firstOrNull()
        assertThat(createdMachine).isNotNull
        assertThat(createdMachine!!.reportedBy).isEqualTo("DR.CODE")

        assertThatThrownBy {
            MachineService.updateMachine(serialNumber = "TEST09", updatedMachine = updatedMachine)
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `getAllMachines() should return machine records successfully`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        val createdMachines = MachineService.getAllMachines()
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(3)
    }

    @Test
    fun `deleteMachine() should delete machine successfully`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))

        MachineService.deleteMachine("TEST01")

        val createdMachines = MachineService.getAllMachines()
        assertThat(createdMachines).isEmpty()

        assertThatThrownBy {
            MachineService.deleteMachine(serialNumber = "TEST09")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }
}
