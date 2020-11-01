package internal

import io.javalin.Javalin
import models.Constants
import services.CacheService
import utils.CookieMonster
import utils.Validator
import utils.logger

object EventHandler {
    fun init(app: Javalin) {
        app.events { event ->
            event.serverStarted { CacheService.start() }
            event.serverStopping { CacheService.stop() }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            app.stop()
        })

        app.after { ctx ->
            if (ctx.status() != 401 &&
                !ctx.path().contains("logout") &&
                CookieMonster.hasCookies(ctx, Constants.USER_TOKEN.name, Constants.USER_NAME.name)) {
                CookieMonster.getCookie(ctx, Constants.USER_TOKEN.name).let { token ->
                    if (Validator.isTokenAlmostExpired(token!!)) {
                        CookieMonster.setCookie(ctx, Constants.USER_TOKEN.name, Validator.generateToken())
                    }
                }
            }
        }

        // TODO add more exception handling w/ proper status codes
        app.exception(Exception::class.java) { e, ctx ->
            logger().info(e.message!!)
            ctx.result(e.message!!)
            ctx.status(500)
        }
    }
}
