package controllers

import io.javalin.http.Context
import models.Constants
import models.User
import services.UserService

object UserController {
    fun createUser(ctx: Context) {
        UserService.createUser(ctx.body<User>())
    }

    fun updateUser(ctx: Context) {
        UserService.updateUser(ctx.pathParam("username"), ctx.body<User>())
    }

    fun updateUsers(ctx: Context) {
        UserService.updateUsers(ctx.bodyAsClass(Array<User>::class.java).toList())
    }

    fun deleteUser(ctx: Context) {
        UserService.deleteUser(ctx.pathParam("username"))
    }

    fun deleteUsers(ctx: Context) {
        UserService.deleteUsers(ctx.bodyAsClass(Array<User>::class.java).toList())
    }

    fun loginUser(ctx: Context) {
        val user = ctx.body<User>()
        val userToken = UserService.loginUser(user)
        ctx.cookie(Constants.USER_TOKEN.name, userToken).cookie(Constants.USER_NAME.name, user.username!!)
    }

    fun logoutUser(ctx: Context) {
        ctx.removeCookie(Constants.USER_TOKEN.name).removeCookie(Constants.USER_NAME.name)
    }

    fun getUsers(ctx: Context) {
        ctx.json(UserService.getUsers())
    }

    fun getUserRoles(ctx: Context) {
        ctx.json(UserService.getUserRoles())
    }
}
