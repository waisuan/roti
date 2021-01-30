package services

import dao.MaintenanceDao
import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import java.time.LocalDateTime
import models.Maintenance
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MaintenanceTable

object MaintenanceService {
    fun getMaintenanceHistory(serialNumber: String, limit: Int = 0, offset: Long = 0, sortFilter: String = "updatedAt", sortOrder: String = "desc"): List<Maintenance> {
        return transaction {
            MaintenanceDao.find { MaintenanceTable.serialNumber eq serialNumber }
                .limit(limit, offset)
                .orderBy(MaintenanceTable.columns.first { it.name == sortFilter } to SortOrder.valueOf(sortOrder.toUpperCase()))
                .map { it.toModel() }
        }
    }

    fun createMaintenanceHistory(serialNumber: String, newMaintenance: Maintenance) {
        if (!newMaintenance.isValid())
            throw BadOperationException(Maintenance::class.java.simpleName)

        transaction {
            if (MaintenanceDao.find {
                    (MaintenanceTable.workOrderNumber eq newMaintenance.workOrderNumber!!).and(
                        MaintenanceTable.serialNumber eq serialNumber
                    )
                }.firstOrNull() != null)
                throw RecordAlreadyExistsException()

            MaintenanceDao.new {
                this.serialNumber = serialNumber
                workOrderNumber = newMaintenance.workOrderNumber!!
                workOrderDate = newMaintenance.workOrderDate
                actionTaken = newMaintenance.actionTaken
                reportedBy = newMaintenance.reportedBy
                workOrderType = newMaintenance.workOrderType
                attachment = newMaintenance.attachment
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
        }
    }

    fun updateMaintenanceHistory(serialNumber: String, workOrderNumber: String, updatedMaintenance: Maintenance) {
        transaction {
            val maintenance = MaintenanceDao.find {
                (MaintenanceTable.workOrderNumber eq workOrderNumber).and(
                    MaintenanceTable.serialNumber eq serialNumber
                )
            }.firstOrNull()
            if (maintenance != null) {
                maintenance.workOrderDate = updatedMaintenance.workOrderDate
                maintenance.actionTaken = updatedMaintenance.actionTaken
                maintenance.reportedBy = updatedMaintenance.reportedBy
                maintenance.workOrderType = updatedMaintenance.workOrderType
                maintenance.attachment = updatedMaintenance.attachment
                maintenance.updatedAt = LocalDateTime.now()
                maintenance.version = updatedMaintenance.version + 1
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    fun deleteMaintenanceHistory(serialNumber: String, workOrderNumber: String) {
        transaction {
            val maintenance = MaintenanceDao.find {
                (MaintenanceTable.workOrderNumber eq workOrderNumber).and(
                    MaintenanceTable.serialNumber eq serialNumber
                )
            }.firstOrNull()
            if (maintenance != null) {
                maintenance.delete()
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    fun searchMaintenanceHistory(serialNumber: String, keyword: String, limit: Int = 0, offset: Long = 0, sortFilter: String = "updatedAt", sortOrder: String = "desc"): List<Maintenance> {
        return transaction {
            MaintenanceDao.find {
                (MaintenanceTable.serialNumber eq serialNumber).and(
                    MaintenanceTable.document.lowerCase() like "%${keyword.toLowerCase()}%"
                )
            }
                .limit(limit, offset)
                .orderBy(MaintenanceTable.columns.first { it.name == sortFilter } to SortOrder.valueOf(sortOrder.toUpperCase()))
                .map { it.toModel() }
        }
    }

    fun getNumberOfMaintenanceRecords(serialNumber: String, keyword: String? = null): Long {
        return transaction {
            if (keyword == null) {
                MaintenanceDao.find { MaintenanceTable.serialNumber eq serialNumber }.count()
            } else {
                MaintenanceDao.find {
                    (MaintenanceTable.serialNumber eq serialNumber).and(
                        MaintenanceTable.document.lowerCase() like "%${keyword.toLowerCase()}%"
                    )
                }.count()
            }
        }
    }
}
