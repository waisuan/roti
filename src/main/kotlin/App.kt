
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
import models.UserRole
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable

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
        }.apply {
            ws("/websocket") { ws ->
                ws.onConnect { }
                ws.onMessage { }
            }
        }.start(System.getenv("PORT")?.toInt() ?: port)

        // Views
        app.get("/register", VueComponent("<register-user></register-user>"), roles(UserRole.GUEST))
        app.get("/login", VueComponent("<login-user></login-user>"), roles(UserRole.GUEST))
        app.get("/admin", VueComponent("<admin-room></admin-room>"), roles(UserRole.ADMIN))

        // API
        app.routes {
            path("api") {
                path("users") {
                    get(UserController::getUsers, roles(UserRole.ADMIN))
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
                    path("roles") {
                        get(UserController::getUserRoles, roles(UserRole.ADMIN))
                    }
                }
                path("machines") {
                    get(MachineController::getAllMachines)
                    post(MachineController::createMachine)
                    path(":serialNumber") {
                        put(MachineController::updateMachine)
                        delete(MachineController::deleteMachine)
                        path("history") {
                            get(MaintenanceController::getMaintenanceHistory)
                            post(MaintenanceController::createMaintenanceHistory)
                            path(":workOrderNumber") {
                                put(MaintenanceController::updateMaintenanceHistory)
                                delete(MaintenanceController::deleteMaintenanceHistory)
                            }
                        }
                    }
                }
                path("files") {
                    path(":ownerId") {
                        get(FileController::getFileNames)
                        post(FileController::saveFile)
                        path(":fileName") {
                            get(FileController::getFile)
                            delete(FileController::deleteFile)
                        }
                    }
                }
            }
        }

        // TODO add more exception handling w/ proper status codes
        app.exception(Exception::class.java) { e, ctx ->
            ctx.result(e.message!!)
            ctx.status(404)
        }

        return app
    }

    private fun initDB() {
        Database.connect(
            url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost/roti",
            driver = "org.postgresql.Driver",
            user = System.getenv("DB_USER") ?: "postgres",
            password = System.getenv("DB_PWD") ?: "password"
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UserTable, MachineTable, MaintenanceTable)
        }
    }
}
