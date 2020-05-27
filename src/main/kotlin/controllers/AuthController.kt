package controllers

import io.javalin.core.security.Role
import io.javalin.core.util.Header
import io.javalin.http.Context
import io.javalin.http.Handler
import models.RotiRole
import utils.Validator

object AuthController {
    fun accessManager(handler: Handler, ctx: Context, permittedRoles: Set<Role>) {
        val token = ctx.header("Authorization")?.removePrefix("Bearer ") ?: ""
        if (isDevMode() || isExcludedFromAuth(permittedRoles) || Validator.verifyToken(token)) {
            handler.handle(ctx)
        } else {
            ctx.header(Header.WWW_AUTHENTICATE, "Basic")
            ctx.status(401)
        }
    }

    private fun isDevMode(): Boolean {
        val devMode = System.getenv("DEV_MODE")
        return devMode != null && devMode == "1"
    }

    private fun isExcludedFromAuth(permittedRoles: Set<Role>): Boolean {
        return RotiRole.ANYONE in permittedRoles
    }
}
