import configs.Config
import controllers.AuthController
import internal.Routes
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import io.javalin.plugin.rendering.vue.VueComponent
import models.Constants
import models.UserRole
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import services.CacheService
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable
import utils.CookieMonster
import utils.Validator
import utils.logger

fun main() {
    RotiApp().init()
}

class RotiApp(private val port: Int = 7000, private val enableDB: Boolean = true) {
    fun init(): Javalin {
        if (enableDB)
            initDB()
        val app = Javalin.create {
            it.accessManager(AuthController::accessManager)
            it.enableCorsForAllOrigins()
            it.enforceSsl = true
            it.enableWebjars()
            it.addStaticFiles("vue/static")
        }.events { event ->
            event.serverStarted { CacheService.start() }
            event.serverStopping { CacheService.stop() }
        }.start(Config.appPort ?: port)

        // Views
        // app.get("/register", VueComponent("<register-user></register-user>"), roles(UserRole.GUEST))
        // app.get("/login", VueComponent("<login-user></login-user>"), roles(UserRole.GUEST))
        app.get("/admin", VueComponent("<admin-room></admin-room>"), roles(UserRole.ADMIN))
        // app.get("/machines", VueComponent("<machine-overview></machine-overview>"), roles(UserRole.NON_ADMIN, UserRole.ADMIN))
        app.error(404, "html", VueComponent("<error-page></error-page>"))

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

        // API
        Routes.init(app)

        // TODO add more exception handling w/ proper status codes
        app.exception(Exception::class.java) { e, ctx ->
            logger().info(e.message!!)
            ctx.result(e.message!!)
            ctx.status(500)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            app.stop()
        })

        return app
    }

    private fun initDB() {
        val dbUrl = Config.dbUrl
        val dbUser = Config.dbUser
        val dbPwd = Config.dbPwd

        Database.connect(
            url = dbUrl,
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPwd
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UserTable, MachineTable, MaintenanceTable)
        }

        Flyway
            .configure()
            .dataSource(dbUrl, dbUser, dbPwd)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
