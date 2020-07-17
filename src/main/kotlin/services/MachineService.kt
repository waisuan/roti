package services

import dao.MachineDao
import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import java.time.LocalDateTime
import models.Machine
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable

object MachineService {
    fun getAllMachines(limit: Int = 0, offset: Long = 0, sortFilter: String = "id", sortOrder: String = "ASC"): List<Machine> {
        return transaction {
            MachineDao.all()
                .limit(limit, offset)
                .orderBy(MachineTable.columns.first { it.name == sortFilter } to SortOrder.valueOf(sortOrder.toUpperCase()))
                .map { it.toModel() }
        }
    }

    fun getNumberOfMachines(): Long {
        return transaction {
            MachineDao.all().count()
        }
    }

    fun createMachine(newMachine: Machine) {
        if (!newMachine.isValid())
            throw BadOperationException(Machine::class.java.simpleName)

        transaction {
            if (MachineDao.find { MachineTable.serialNumber eq newMachine.serialNumber!! }.firstOrNull() != null)
                throw RecordAlreadyExistsException()

            MachineDao.new {
                serialNumber = newMachine.serialNumber!!
                customer = newMachine.customer
                state = newMachine.state
                accountType = newMachine.accountType
                model = newMachine.model
                status = newMachine.status
                brand = newMachine.brand
                district = newMachine.district
                personInCharge = newMachine.personInCharge
                reportedBy = newMachine.reportedBy
                additionalNotes = newMachine.additionalNotes
                tncDate = newMachine.tncDate
                ppmDate = newMachine.ppmDate
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
        }
    }

    fun updateMachine(serialNumber: String, updatedMachine: Machine) {
        transaction {
            val machine = MachineDao.find { MachineTable.serialNumber eq serialNumber }.firstOrNull()
            if (machine != null) {
                machine.customer = updatedMachine.customer
                machine.state = updatedMachine.state
                machine.accountType = updatedMachine.accountType
                machine.model = updatedMachine.model
                machine.status = updatedMachine.status
                machine.brand = updatedMachine.brand
                machine.district = updatedMachine.district
                machine.personInCharge = updatedMachine.personInCharge
                machine.reportedBy = updatedMachine.reportedBy
                machine.additionalNotes = updatedMachine.additionalNotes
                machine.tncDate = updatedMachine.tncDate
                machine.ppmDate = updatedMachine.ppmDate
                machine.updatedAt = LocalDateTime.now()
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    fun deleteMachine(serialNumber: String) {
        transaction {
            val machine = MachineDao.find { MachineTable.serialNumber eq serialNumber }.firstOrNull()
            if (machine != null) {
                machine.delete()
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    fun searchMachine(keyword: String): List<Machine> {
        return transaction {
            MachineDao.find { MachineTable.document.lowerCase() like "%${keyword.toLowerCase()}%" }.map {
                it.toModel()
            }
        }
    }
}
