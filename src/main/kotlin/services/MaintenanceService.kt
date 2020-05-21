package services

import dao.MaintenanceDao
import exceptions.RecordNotFoundException
import java.time.LocalDateTime
import models.Maintenance
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MaintenanceTable

object MaintenanceService {
    fun getMaintenanceHistory(serialNumber: String): List<Maintenance> {
        return transaction {
            MaintenanceDao.find { MaintenanceTable.serialNumber eq serialNumber }.map { it.toModel() }
        }
    }

    fun createMaintenanceHistory(serialNumber: String, newMaintenance: Maintenance) {
        transaction {
            MaintenanceDao.new {
                this.serialNumber = serialNumber
                workOrderNumber = newMaintenance.workOrderNumber!!
                workOrderDate = newMaintenance.workOrderDate
                actionTaken = newMaintenance.actionTaken
                reportedBy = newMaintenance.reportedBy
                workOrderType = newMaintenance.workOrderType
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
        }
    }

    fun updateMaintenanceHistory(workOrderNumber: String, updatedMaintenance: Maintenance) {
        transaction {
            val maintenance = MaintenanceDao.find { MaintenanceTable.workOrderNumber eq workOrderNumber }.firstOrNull()
            if (maintenance != null) {
                maintenance.workOrderDate = updatedMaintenance.workOrderDate
                maintenance.actionTaken = updatedMaintenance.actionTaken
                maintenance.reportedBy = updatedMaintenance.reportedBy
                maintenance.workOrderType = updatedMaintenance.workOrderType
                maintenance.updatedAt = LocalDateTime.now()
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    fun deleteMaintenanceHistory(workOrderNumber: String) {
        transaction {
            val maintenance = MaintenanceDao.find { MaintenanceTable.workOrderNumber eq workOrderNumber }.firstOrNull()
            if (maintenance != null) {
                maintenance.delete()
            } else {
                throw RecordNotFoundException()
            }
        }
    }
}
