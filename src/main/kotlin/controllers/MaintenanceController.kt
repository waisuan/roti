package controllers

import io.javalin.http.Context
import models.Maintenance
import services.MaintenanceService

object MaintenanceController {
    fun getMaintenanceHistory(ctx: Context) {
        ctx.json(MaintenanceService.getMaintenanceHistory(ctx.pathParam("serialNumber")))
    }

    fun createMaintenanceHistory(ctx: Context) {
        MaintenanceService.createMaintenanceHistory(ctx.pathParam("serialNumber"), ctx.body<Maintenance>())
    }

    fun updateMaintenanceHistory(ctx: Context) {
        MaintenanceService.updateMaintenanceHistory(ctx.pathParam("workOrderNumber"), ctx.body<Maintenance>())
    }

    fun deleteMaintenanceHistory(ctx: Context) {
        MaintenanceService.deleteMaintenanceHistory(ctx.pathParam("workOrderNumber"))
    }
}
