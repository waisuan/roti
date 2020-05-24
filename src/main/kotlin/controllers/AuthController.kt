package controllers

import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import utils.Validator

object AuthController {
    fun accessManager(handler: Handler, ctx: Context, permittedRoles: Set<Role>) {
        val token = ctx.header("Authorization")?.removePrefix("Bearer ") ?: ""
        if (isDevMode() || isExcludedFromAuth(ctx.matchedPath(), ctx.method()) || Validator.verifyToken(token)) {
            handler.handle(ctx)
        } else {
            ctx.status(401)
        }
    }

    private fun isDevMode(): Boolean {
        val devMode = System.getenv("DEV_MODE")
        return devMode != null && devMode == "1"
    }

    private fun isExcludedFromAuth(path: String, method: String): Boolean {
        return method == "POST" && (path == "/users/login" || path == "/users/register")
    }
}
