package controllers

import io.javalin.http.Context
import models.Constants
import models.User
import models.UserRole
import services.UserService
import utils.CookieMonster

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
        UserService.deleteUsers(ctx.queryParams("users"))
    }

    fun loginUser(ctx: Context) {
        val user = ctx.body<User>()
        val userToken = UserService.loginUser(user)
        val isAdmin = (UserService.getUser(user.username!!)!!.role == UserRole.ADMIN)

        CookieMonster.setCookie(ctx, Constants.USER_TOKEN.name, userToken)
        CookieMonster.setCookie(ctx, Constants.USER_NAME.name, user.username)
        ctx.json(isAdmin)
    }

    fun logoutUser(ctx: Context) {
        CookieMonster.removeCookie(ctx, Constants.USER_TOKEN.name)
        CookieMonster.removeCookie(ctx, Constants.USER_NAME.name)
    }

    fun getUsers(ctx: Context) {
        ctx.json(UserService.getUsers())
    }

    fun getUserRoles(ctx: Context) {
        ctx.json(UserService.getUserRoles())
    }
}
