package controllers

import io.javalin.http.Context
import io.javalin.http.context.body
import jobs.EmailJob
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.Constants
import models.User
import services.EmailService
import services.UserService
import utils.CookieMonster

object UserController {
    fun createUser(ctx: Context) {
        val userBody = ctx.body<User>()
        UserService.createUser(userBody).also {
            GlobalScope.launch {
                EmailJob.perform { EmailService.sendRegistrationSuccessful(userBody.username!!, userBody.email!!) }
            }
        }
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
        val loggedInUser = UserService.getUser(user.username!!)!!
        loggedInUser.token = userToken

        CookieMonster.setCookie(ctx, Constants.USER_TOKEN.name, userToken)
        CookieMonster.setCookie(ctx, Constants.USER_NAME.name, user.username)
        ctx.json(loggedInUser)
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
