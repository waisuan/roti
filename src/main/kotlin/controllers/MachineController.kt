package controllers

import io.javalin.http.Context
import models.Machine
import services.MachineService

object MachineController {
    fun getAllMachines(ctx: Context) {
        ctx.json(MachineService.getAllMachines())
    }

    fun createMachine(ctx: Context) {
        MachineService.createMachine(ctx.body<Machine>())
    }

    fun updateMachine(ctx: Context) {
        MachineService.updateMachine(ctx.pathParam("serialNumber"), ctx.body<Machine>())
    }
}
