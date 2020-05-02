package controllers

import io.javalin.http.Context
import models.User
import services.UserService
import utils.Validator

object UserController {
    fun getAllUsers(ctx: Context) {
        ctx.json(UserService.getAllUsers())
    }

    fun createUser(ctx: Context) {
        UserService.createUser(ctx.body<User>())
    }

    fun loginUser(ctx: Context) {
        if (!UserService.loginUser(ctx.body<User>()))
            ctx.status(401)
        ctx.json(Validator.generateToken())
    }
}
