package dao

import models.Maintenance
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tables.MaintenanceTable

class MaintenanceDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MaintenanceDao>(MaintenanceTable)

    var serialNumber by MaintenanceTable.serialNumber
    var workOrderNumber by MaintenanceTable.workOrderNumber
    var workOrderDate by MaintenanceTable.workOrderDate
    var actionTaken by MaintenanceTable.actionTaken
    var reportedBy by MaintenanceTable.reportedBy
    var workOrderType by MaintenanceTable.workOrderType
    var attachment by MaintenanceTable.attachment
    var createdAt by MaintenanceTable.createdAt
    var updatedAt by MaintenanceTable.updatedAt

    fun toModel(): Maintenance {
        return Maintenance(
            serialNumber,
            workOrderNumber,
            workOrderDate,
            actionTaken,
            reportedBy,
            workOrderType,
            attachment,
            createdAt,
            updatedAt
        )
    }
}
