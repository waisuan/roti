package dao

import models.Machine
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tables.MachineTable

class MachineDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MachineDao>(MachineTable)

    var serialNumber by MachineTable.serialNumber
    var customer by MachineTable.customer
    var state by MachineTable.state
    var accountType by MachineTable.accountType
    var model by MachineTable.model
    var status by MachineTable.status
    var brand by MachineTable.brand
    var district by MachineTable.district
    var personInCharge by MachineTable.personInCharge
    var reportedBy by MachineTable.reportedBy
    var additionalNotes by MachineTable.additionalNotes
    var attachment by MachineTable.attachment
    var tncDate by MachineTable.tncDate
    var ppmDate by MachineTable.ppmDate
    var createdAt by MachineTable.createdAt
    var updatedAt by MachineTable.updatedAt
    var version by MachineTable.version

    fun toModel(ppmStatus: String? = null): Machine {
        return Machine(
            serialNumber,
            customer,
            state,
            accountType,
            model,
            status,
            brand,
            district,
            personInCharge,
            reportedBy,
            additionalNotes,
            attachment,
            ppmStatus,
            tncDate,
            ppmDate,
            createdAt,
            updatedAt,
            version
        )
    }
}
