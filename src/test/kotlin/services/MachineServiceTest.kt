package services

import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import helpers.TestDatabase
import java.time.LocalDate
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.exceptions.ExposedSQLException
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
        MachineService.createMachine(Machine(serialNumber = "TEST01"))

        var createdMachine = MachineService.getAllMachines().firstOrNull()
        assertThat(createdMachine).isNotNull
        assertThat(createdMachine!!.reportedBy).isNull()

        val updatedMachine = Machine(serialNumber = createdMachine.serialNumber, reportedBy = "DR.CODE", updatedAt = createdMachine.updatedAt)
        MachineService.updateMachine(serialNumber = createdMachine.serialNumber!!, updatedMachine = updatedMachine)

        createdMachine = MachineService.getAllMachines().first()
        assertThat(createdMachine.reportedBy).isEqualTo("DR.CODE")

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
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST03")

        createdMachines = MachineService.getAllMachines(limit = 1, offset = 1)
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(1)
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST02")

        createdMachines = MachineService.getAllMachines(limit = 1, offset = 2)
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.size).isEqualTo(1)
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST01")

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
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST01")
        assertThat(createdMachines[1].serialNumber).isEqualTo("TEST03")
        assertThat(createdMachines.last().serialNumber).isEqualTo("TEST02")

        createdMachines = MachineService.getAllMachines(sortFilter = "state", sortOrder = "ASC")
        assertThat(createdMachines).isNotEmpty
        assertThat(createdMachines.first().serialNumber).isEqualTo("TEST02")
        assertThat(createdMachines[1].serialNumber).isEqualTo("TEST03")
        assertThat(createdMachines.last().serialNumber).isEqualTo("TEST01")
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
        assertThat(machines.map { it.serialNumber }).isEqualTo(listOf(machine3.serialNumber, machine2.serialNumber))

        machines = MachineService.searchMachine("something")
        assertThat(machines.map { it.serialNumber }).isEqualTo(emptyList<String>())
    }

    @Test
    fun `searchMachine() with limit & offset should return search results as expected`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        var found = MachineService.searchMachine(keyword = "TEST", limit = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().serialNumber).isEqualTo("TEST03")

        found = MachineService.searchMachine(keyword = "TEST", limit = 1, offset = 1)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().serialNumber).isEqualTo("TEST02")

        found = MachineService.searchMachine(keyword = "TEST", limit = 1, offset = 2)
        assertThat(found).isNotEmpty
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first().serialNumber).isEqualTo("TEST01")

        found = MachineService.searchMachine(keyword = "TEST", limit = 1, offset = 3)
        assertThat(found).isEmpty()
    }

    @Test
    fun `searchMachine() should return search results in a specified order`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01", state = "C"))
        MachineService.createMachine(Machine(serialNumber = "TEST02", state = "A"))
        MachineService.createMachine(Machine(serialNumber = "TEST03", state = "B"))

        var found = MachineService.searchMachine(keyword = "TEST", sortFilter = "state")
        assertThat(found).isNotEmpty
        assertThat(found.first().serialNumber).isEqualTo("TEST01")
        assertThat(found[1].serialNumber).isEqualTo("TEST03")
        assertThat(found.last().serialNumber).isEqualTo("TEST02")

        found = MachineService.searchMachine(keyword = "TEST", sortFilter = "state", sortOrder = "ASC")
        assertThat(found).isNotEmpty
        assertThat(found.first().serialNumber).isEqualTo("TEST02")
        assertThat(found[1].serialNumber).isEqualTo("TEST03")
        assertThat(found.last().serialNumber).isEqualTo("TEST01")
    }

    @Test
    fun `getNumberOfMachines() returns number of machines in the DB`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))
        MachineService.createMachine(Machine(serialNumber = "TEST02"))
        MachineService.createMachine(Machine(serialNumber = "TEST03"))

        assertThat(MachineService.getNumberOfMachines()).isEqualTo(3)

        assertThat(MachineService.getNumberOfMachines(keyword = "TEST02")).isEqualTo(1)
    }

    @Test
    fun `getNumOfPpmDueMachines() returns the right number of due machines`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01", ppmDate = LocalDate.parse("2020-01-01")))
        MachineService.createMachine(Machine(serialNumber = "TEST02", ppmDate = LocalDate.parse("2020-07-10")))
        MachineService.createMachine(Machine(serialNumber = "TEST03", ppmDate = LocalDate.parse("2020-07-14")))
        MachineService.createMachine(Machine(serialNumber = "TEST04", ppmDate = LocalDate.parse("2020-08-08")))

        assertThat(MachineService.getNumOfPpmDueMachines(LocalDate.parse("2020-07-28"))).isEqualTo(2)
    }

    @Test
    fun `getPpmDueMachines() returns machine records that are over,almost,are due for servicing`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01", ppmDate = LocalDate.parse("2020-01-01")))
        MachineService.createMachine(Machine(serialNumber = "TEST02", ppmDate = LocalDate.parse("2020-07-10")))
        MachineService.createMachine(Machine(serialNumber = "TEST03", ppmDate = LocalDate.parse("2020-07-14")))
        MachineService.createMachine(Machine(serialNumber = "TEST04", ppmDate = LocalDate.parse("2020-07-28")))
        MachineService.createMachine(Machine(serialNumber = "TEST05", ppmDate = LocalDate.parse("2020-08-08")))
        MachineService.createMachine(Machine(serialNumber = "TEST06", ppmDate = LocalDate.parse("2020-08-12")))

        val ppmDueMachines = MachineService.getPpmDueMachines(LocalDate.parse("2020-07-28")).sortedBy { it.serialNumber }
        assertThat(ppmDueMachines.size).isEqualTo(3)
        assertThat(ppmDueMachines.map { Pair(it.serialNumber, it.ppmStatus) }.first()).isEqualTo(Pair("TEST03", "overdue"))
        assertThat(ppmDueMachines.map { Pair(it.serialNumber, it.ppmStatus) }[1]).isEqualTo(Pair("TEST04", "due"))
        assertThat(ppmDueMachines.map { Pair(it.serialNumber, it.ppmStatus) }.last()).isEqualTo(Pair("TEST05", "almost_due"))
    }

    @Test
    fun `getPpmDueMachines() returns no records if none are due for servicing`() {
        assertThat(MachineService.getPpmDueMachines(LocalDate.parse("2020-07-28"))).isEqualTo(emptyList<Machine>())
    }

    @Test
    fun `Concurrent updates should not override one another`() {
        MachineService.createMachine(Machine(serialNumber = "TEST01"))

        val machines = MachineService.getAllMachines()
        val machine1 = machines.first()
        val machine2 = machines.first()

        MachineService.updateMachine(machine1.serialNumber!!, machine1)
        assertThatThrownBy { MachineService.updateMachine(machine2.serialNumber!!, machine2) }
            .isInstanceOf(ExposedSQLException::class.java)
            .hasMessageContaining("Updated record looks outdated.")
    }

    @Test
    fun `createMachine() should stamp createdAt and updatedAt fields`() {
        val newMachine = Machine(serialNumber = "TEST01")
        MachineService.createMachine(newMachine)

        MachineService.getAllMachines().first().let {
            assertThat(it.createdAt).isNotNull
            assertThat(it.updatedAt).isNotNull
            assertThat(it.createdAt).isEqualTo(it.updatedAt)
        }
    }

    @Test
    fun `updateMachine() should refresh updatedAt field`() {
        val newMachine = Machine(serialNumber = "TEST01")

        MachineService.createMachine(newMachine)
        val machine = MachineService.getAllMachines().first().let {
            assertThat(it.createdAt).isEqualTo(it.updatedAt)
            it
        }

        MachineService.updateMachine(machine.serialNumber!!, machine.copy(reportedBy = "ME"))
        MachineService.getAllMachines().first().let {
            assertThat(it.updatedAt).isNotNull
            assertThat(it.createdAt).isNotEqualTo(it.updatedAt)
        }
    }

    @Test
    fun `Version column for new Machines start at 1`() {
        val newMachine = Machine(serialNumber = "TEST01")
        MachineService.createMachine(newMachine)

        MachineService.getAllMachines().first().let {
            assertThat(it.version).isEqualTo(1)
        }
    }

    @Test
    fun `Version column on Machine record gets incremented at every update`() {
        val machine = Machine(serialNumber = "TEST01")

        MachineService.createMachine(machine)
        MachineService.getAllMachines().first().let {
            assertThat(it.version).isEqualTo(1)
        }

        MachineService.updateMachine(machine.serialNumber!!, machine.copy(reportedBy = "ME"))
        MachineService.getAllMachines().first().let {
            assertThat(it.version).isEqualTo(2)
        }
    }
}
