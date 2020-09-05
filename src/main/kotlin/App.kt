
import controllers.AuthController
import controllers.FileController
import controllers.MachineController
import controllers.MaintenanceController
import controllers.UserController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put
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
import services.MachineService
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
        }.apply {
            ws("/websocket") { ws ->
                ws.onConnect { ctx ->
                    while (true) {
                        ctx.send(MachineService.getNumOfPpmDueMachines())
                        Thread.sleep(3600000L) // 1.hour
                    }
                }
                ws.onMessage { }
            }
        }.events { event ->
            event.serverStarted { CacheService.start() }
            event.serverStopping { CacheService.stop() }
        }.start(System.getenv("PORT")?.toInt() ?: port)

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
                val token = CookieMonster.getCookie(ctx, Constants.USER_TOKEN.name)
                if (Validator.isTokenAlmostExpired(token!!)) {
                    CookieMonster.setCookie(ctx, Constants.USER_TOKEN.name, Validator.generateToken())
                }
            }
        }

        // API
        app.routes {
            path("api") {
                path("users") {
                    get(UserController::getUsers, roles(UserRole.ADMIN))
                    put(UserController::updateUsers, roles(UserRole.ADMIN))
                    delete(UserController::deleteUsers, roles(UserRole.ADMIN))
                    path(":username") {
                        put(UserController::updateUser, roles(UserRole.ADMIN))
                        delete(UserController::deleteUser, roles(UserRole.ADMIN))
                    }
                    path("register") {
                        post(UserController::createUser, roles(UserRole.GUEST))
                    }
                    path("login") {
                        post(UserController::loginUser, roles(UserRole.GUEST))
                    }
                    path("logout") {
                        post(UserController::logoutUser, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                    }
                    path("roles") {
                        get(UserController::getUserRoles, roles(UserRole.ADMIN))
                    }
                }
                path("machines") {
                    get(MachineController::getAllMachines, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                    post(MachineController::createMachine, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                    path(":serialNumber") {
                        put(MachineController::updateMachine, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        delete(MachineController::deleteMachine, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        path("history") {
                            get(MaintenanceController::getMaintenanceHistory, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                            post(
                                MaintenanceController::createMaintenanceHistory, roles(
                                    UserRole.ADMIN,
                                    UserRole.NON_ADMIN
                                )
                            )
                            path(":workOrderNumber") {
                                put(
                                    MaintenanceController::updateMaintenanceHistory, roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                                delete(
                                    MaintenanceController::deleteMaintenanceHistory, roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                            }
                            path("search") {
                                path(":keyword") {
                                    get(
                                        MaintenanceController::searchMaintenanceHistory, roles(
                                            UserRole.ADMIN,
                                            UserRole.NON_ADMIN
                                        )
                                    )
                                }
                            }
                            path("count") {
                                get(
                                    MaintenanceController::getNumberOfRecords,
                                    roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                                )
                            }
                        }
                    }
                    path("search") {
                        path(":keyword") {
                            get(MachineController::searchMachine, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        }
                    }
                    path("count") {
                        get(MachineController::getNumberOfMachines, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                    }
                    path("due") {
                        get(MachineController::getPpmDueMachines, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        path("count") {
                            get(MachineController::getNumOfPpmDueMachines, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        }
                    }
                }
                path("files") {
                    path(":ownerId") {
                        get(FileController::getFileNames, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        post(FileController::saveFile, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        path(":fileName") {
                            get(FileController::getFile, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                            delete(FileController::deleteFile, roles(UserRole.ADMIN, UserRole.NON_ADMIN))
                        }
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

        Runtime.getRuntime().addShutdownHook(Thread {
            app.stop()
        })

        return app
    }

    private fun initDB() {
        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost/roti"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPwd = System.getenv("DB_PWD") ?: "password"

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

        val flyway = Flyway
            .configure()
            .dataSource(dbUrl, dbUser, dbPwd)
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }
}
