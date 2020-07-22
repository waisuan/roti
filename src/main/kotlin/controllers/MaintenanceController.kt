package controllers

import io.javalin.http.Context
import models.Maintenance
import services.MaintenanceService

object MaintenanceController {
    fun getMaintenanceHistory(ctx: Context) {
        ctx.json(MaintenanceService.getMaintenanceHistory(
            ctx.pathParam("serialNumber"),
            ctx.queryParam("page_limit", "0")!!.toInt(),
            ctx.queryParam("page_offset", "0")!!.toLong(),
            ctx.queryParam("sort_filter", "id")!!,
            ctx.queryParam("sort_order", "ASC")!!
        ))
    }

    fun createMaintenanceHistory(ctx: Context) {
        MaintenanceService.createMaintenanceHistory(ctx.pathParam("serialNumber"), ctx.body<Maintenance>())
    }

    fun updateMaintenanceHistory(ctx: Context) {
        MaintenanceService.updateMaintenanceHistory(ctx.pathParam("serialNumber"), ctx.pathParam("workOrderNumber"), ctx.body<Maintenance>())
    }

    fun deleteMaintenanceHistory(ctx: Context) {
        MaintenanceService.deleteMaintenanceHistory(ctx.pathParam("serialNumber"), ctx.pathParam("workOrderNumber"))
    }

    fun searchMaintenanceHistory(ctx: Context) {
        ctx.json(MaintenanceService.searchMaintenanceHistory(
            ctx.pathParam("serialNumber"),
            ctx.pathParam("keyword"),
            ctx.queryParam("page_limit", "0")!!.toInt(),
            ctx.queryParam("page_offset", "0")!!.toLong(),
            ctx.queryParam("sort_filter", "id")!!,
            ctx.queryParam("sort_order", "ASC")!!
        ))
    }

    fun getNumberOfRecords(ctx: Context) {
        ctx.json(MaintenanceService.getNumberOfMaintenanceRecords(ctx.pathParam("serialNumber"), ctx.queryParam("keyword")))
    }
}
