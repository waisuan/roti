package controllers

import io.javalin.http.Context
import io.javalin.http.context.body
import models.Machine
import models.MachineContainer
import services.MachineService

object MachineController {
    fun getAllMachines(ctx: Context) {
        ctx.json(MachineContainer(
            machines = MachineService.getAllMachines(
                ctx.queryParam("page_limit", "0")!!.toInt(),
                ctx.queryParam("page_offset", "0")!!.toLong(),
                ctx.queryParam("sort_filter", "updatedAt")!!,
                ctx.queryParam("sort_order", "desc")!!
            ),
            count = MachineService.getNumberOfMachines()
        ))
    }

    fun createMachine(ctx: Context) {
        ctx.json(MachineService.createMachine(ctx.body<Machine>()))
    }

    fun updateMachine(ctx: Context) {
        ctx.json(MachineService.updateMachine(ctx.pathParam("serialNumber"), ctx.body<Machine>()))
    }

    fun deleteMachine(ctx: Context) {
        MachineService.deleteMachine(ctx.pathParam("serialNumber"))
    }

    fun searchMachine(ctx: Context) {
        ctx.json(MachineContainer(
            machines = MachineService.searchMachine(
                ctx.pathParam("keyword"),
                ctx.queryParam("page_limit", "0")!!.toInt(),
                ctx.queryParam("page_offset", "0")!!.toLong(),
                ctx.queryParam("sort_filter", "updatedAt")!!,
                ctx.queryParam("sort_order", "desc")!!
            ),
            count = MachineService.getNumberOfMachines(ctx.pathParam("keyword"))
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
