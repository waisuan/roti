package utils

import io.javalin.http.Context
import io.javalin.http.util.ContextUtil.isLocalhost

object CookieMonster {
    fun setCookie(ctx: Context, key: String, value: String) {
        if (isSecure(ctx)) {
            ctx.res.addHeader("Set-Cookie", "$key=$value; Path=/; HttpOnly; Secure; SameSite=none")
        } else {
            ctx.cookie(key, value)
        }
    }

    fun getCookie(ctx: Context, key: String): String? {
        return ctx.cookie(key)
    }

    fun removeCookie(ctx: Context, key: String) {
        if (isSecure(ctx)) {
            ctx.res.addHeader("Set-Cookie", "$key=; Path=/; HttpOnly; Secure; SameSite=none; Max-Age=0")
        } else {
            ctx.removeCookie(key, "/")
        }
    }

    fun hasCookie(ctx: Context, key: String): Boolean {
        return !ctx.cookie(key).isNullOrEmpty()
    }

    fun hasCookies(ctx: Context, vararg keys: String): Boolean {
        for (key in keys) {
            if (!hasCookie(ctx, key)) {
                return false
            }
        }
        return true
    }

    private fun isSecure(ctx: Context): Boolean {
        return !ctx.isLocalhost()
    }
}
