package tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime

object MachineTable : LongIdTable("Machines") {
    val serialNumber = varchar("serialNumber", 100).uniqueIndex()
    val customer = varchar("customer", 200).nullable()
    val state = varchar("state", 100).nullable()
    val accountType = varchar("accountType", 100).nullable()
    val model = varchar("model", 100).nullable()
    val status = varchar("status", 100).nullable()
    val brand = varchar("brand", 100).nullable()
    val district = varchar("district", 100).nullable()
    val personInCharge = varchar("personInCharge", 200).nullable()
    val reportedBy = varchar("reportedBy", 200).nullable()
    val additionalNotes = text("additionalNotes").nullable()
    val tncDate = date("tncDate").nullable()
    val ppmDate = date("ppmDate").nullable()
    val createdAt = datetime("createdAt")
    val updatedAt = datetime("updatedAt")
    val document = text("document").nullable()
    val attachment = varchar("attachment", 200).nullable()
    val version = integer("version").default(1)
}
