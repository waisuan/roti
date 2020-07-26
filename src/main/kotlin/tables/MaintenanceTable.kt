package tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import tables.MachineTable.nullable

object MaintenanceTable : LongIdTable("Maintenance") {
    val serialNumber = (varchar("serialNumber", 100) references MachineTable.serialNumber)
    val workOrderNumber = varchar("workOrderNumber", 100)
    val workOrderDate = date("workOrderDate").nullable()
    val actionTaken = text("actionTaken").nullable()
    val reportedBy = varchar("reportedBy", 200).nullable()
    val workOrderType = varchar("workOrderType", 100).nullable()
    val createdAt = datetime("createdAt")
    val updatedAt = datetime("updatedAt")
    val document = text("document").nullable()
    val attachment = varchar("attachment", 200).nullable()

    init {
        index(true, serialNumber, workOrderNumber)
    }
}
