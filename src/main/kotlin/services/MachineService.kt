package services

import dao.MachineDao
import exceptions.RecordNotFoundException
import java.time.LocalDateTime
import models.Machine
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable

object MachineService {
    fun getAllMachines(): List<Machine> {
        return transaction {
            MachineDao.all().map { it.toModel() }
        }
    }

    fun createMachine(newMachine: Machine) {
        transaction {
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
}
