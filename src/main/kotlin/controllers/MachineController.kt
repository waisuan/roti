package controllers

import io.javalin.http.Context
import models.Machine
import services.MachineService

object MachineController {
    fun getAllMachines(ctx: Context) {
        ctx.json(MachineService.getAllMachines(
            ctx.queryParam("page_limit", "0")!!.toInt(),
            ctx.queryParam("page_offset", "0")!!.toLong(),
            ctx.queryParam("sort_filter", "id")!!,
            ctx.queryParam("sort_order", "ASC")!!
        ))
    }

    fun createMachine(ctx: Context) {
        MachineService.createMachine(ctx.body<Machine>())
    }

    fun updateMachine(ctx: Context) {
        MachineService.updateMachine(ctx.pathParam("serialNumber"), ctx.body<Machine>())
    }

    fun deleteMachine(ctx: Context) {
        MachineService.deleteMachine(ctx.pathParam("serialNumber"))
    }

    fun searchMachine(ctx: Context) {
        ctx.json(MachineService.searchMachine(
            ctx.pathParam("keyword"),
            ctx.queryParam("page_limit", "0")!!.toInt(),
            ctx.queryParam("page_offset", "0")!!.toLong(),
            ctx.queryParam("sort_filter", "id")!!,
            ctx.queryParam("sort_order", "ASC")!!
        ))
    }

    fun getNumberOfMachines(ctx: Context) {
        ctx.json(MachineService.getNumberOfMachines(ctx.queryParam("keyword")))
    }

    fun getNumOfPpmDueMachines(ctx: Context) {
        ctx.json(MachineService.getNumOfPpmDueMachines())
    }

    fun getPpmDueMachines(ctx: Context) {
        ctx.json(MachineService.getPpmDueMachines())
    }
}
