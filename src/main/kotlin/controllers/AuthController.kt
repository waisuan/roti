package controllers

import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import models.Constants
import models.UserRole
import services.UserService
import utils.Validator
import utils.logger

object AuthController {
    fun accessManager(handler: Handler, ctx: Context, permittedRoles: Set<Role>) {
        val token = ctx.cookie(Constants.USER_TOKEN.name) ?: ""
        val username = ctx.cookie(Constants.USER_NAME.name) ?: ""
        logger().info(ctx.cookieMap().toString())
        if (isDevMode() || isExcludedFromAuth(permittedRoles) || (isAuthorizedRole(username, permittedRoles) && Validator.verifyToken(token))) {
            handler.handle(ctx)
        } else {
            ctx.status(401)
        }
    }

    private fun isDevMode(): Boolean {
        val devMode = System.getenv("DEV_MODE")
        return devMode != null && devMode == "1"
    }

    private fun isExcludedFromAuth(permittedRoles: Set<Role>): Boolean {
        return UserRole.GUEST in permittedRoles
    }

    private fun isAuthorizedRole(username: String, permittedRoles: Set<Role>): Boolean {
        if (permittedRoles.isEmpty())
            return true
        val user = UserService.getUser(username) ?: return false
        return user.role!! in permittedRoles
    }
}
