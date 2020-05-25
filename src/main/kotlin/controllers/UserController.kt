package controllers

import io.javalin.http.Context
import models.User
import services.UserService

object UserController {
    fun createUser(ctx: Context) {
        UserService.createUser(ctx.body<User>())
    }

    fun updateUser(ctx: Context) {
        UserService.updateUser(ctx.pathParam("username"), ctx.body<User>())
    }

    fun deleteUser(ctx: Context) {
        UserService.deleteUser(ctx.pathParam("username"))
    }

    fun loginUser(ctx: Context) {
        ctx.json(UserService.loginUser(ctx.body<User>()))
    }
}
