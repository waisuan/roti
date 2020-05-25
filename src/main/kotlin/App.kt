
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable

fun main() {
    init()
    val app = Javalin.create {
        it.accessManager(AuthController::accessManager)
        it.enableCorsForAllOrigins()
        it.enforceSsl = true
    }.apply {
        ws("/websocket") { ws ->
            ws.onConnect { }
            ws.onMessage { }
        }
    }.start(System.getenv("PORT")?.toInt() ?: 7000)

    app.routes {
        path("users") {
            path(":username") {
                put(UserController::updateUser)
                delete(UserController::deleteUser)
            }
            path("register") {
                post(UserController::createUser)
            }
            path("login") {
                post(UserController::loginUser)
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

    app.exception(Exception::class.java) { e, ctx ->
        ctx.result(e.message!!)
        ctx.status(404)
    }
}

fun init() {
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
