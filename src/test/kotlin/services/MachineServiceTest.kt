package services

import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import helpers.TestDatabase
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MachineServiceTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `createMachine() should create a new machine record successfully`() {
        var newMachine = Machine(serialNumber = "TEST01")
        MachineService.createMachine(newMachine)

        val createdMachine = MachineService.getAllMachines().firstOrNull()
        assertThat(createdMachine).isNotNull
        assertThat(createdMachine!!.serialNumber).isEqualTo("TEST01")

        assertThatThrownBy {
            MachineService.createMachine(newMachine)
        }.isInstanceOf(RecordAlreadyExistsException::class.java)

        newMachine = Machine()
        assertThatThrownBy {
            MachineService.createMachine(newMachine)
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on Machine record")
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
    fun `getAllMachines() with limit & offset should return machine records successfully`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        var createdMachines = MachineService.getAllMachines(limit = 1)
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(1)
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST01")

        createdMachines = MachineService.getAllMachines(limit = 1, offset = 1)
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(1)
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST02")

        createdMachines = MachineService.getAllMachines(limit = 1, offset = 2)
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(1)
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST03")

        createdMachines = MachineService.getAllMachines(limit = 1, offset = 3)
        assertThat(createdMachines).isEmpty()
    }

    @Test
    fun `getAllMachines() should return machine records in a specified order`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01", state = "C"))
        MachineService.createMachine(Machine(serialNumber = "TEST02", state = "A"))
        MachineService.createMachine(Machine(serialNumber = "TEST03", state = "B"))

        var createdMachines = MachineService.getAllMachines(sortFilter = "state")
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST02")
        assertThat(createdMachines[1].serialNumber).isEqualTo("TEST03")
        assertThat(createdMachines.last().serialNumber).isEqualTo("TEST01")

        createdMachines = MachineService.getAllMachines(sortFilter = "state", sortOrder = "DESC")
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST01")
        assertThat(createdMachines[1].serialNumber).isEqualTo("TEST03")
        assertThat(createdMachines.last().serialNumber).isEqualTo("TEST02")
    }

    @Test
    fun `deleteMachine() should delete machine record successfully`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))

        MachineService.deleteMachine("TEST01")

        val createdMachines = MachineService.getAllMachines()
        assertThat(createdMachines).isEmpty()

        assertThatThrownBy {
            MachineService.deleteMachine(serialNumber = "TEST09")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `searchMachine() should return search results as expected`() {
        val machine1 = Machine(serialNumber = "TEST01", state = "Regalia")
        val machine2 = Machine(serialNumber = "TEST02", customer = "Noctis, Sir", district = "NotImperial")
        val machine3 = Machine(serialNumber = "TEST03", reportedBy = "Prompto", brand = "Imperial")
        MachineService.createMachine(machine1)
        MachineService.createMachine(machine2)
        MachineService.createMachine(machine3)

        var machines = MachineService.searchMachine("sir")
        assertThat(machines.map { it.serialNumber }).isEqualTo(listOf(machine2.serialNumber))

        machines = MachineService.searchMachine("perial")
        assertThat(machines.map { it.serialNumber }).isEqualTo(listOf(machine2.serialNumber, machine3.serialNumber))

        machines = MachineService.searchMachine("something")
        assertThat(machines.map { it.serialNumber }).isEqualTo(emptyList<String>())
    }

    @Test
    fun `getNumberOfMachines() returns number of machines in the DB`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        assertThat(MachineService.getNumberOfMachines()).isEqualTo(3)
    }
}
