
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
            get(UserController::getAllUsers)
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
                post(FileController::saveFile)
                // get
                path(":filename") {
                    get(FileController::getFile)
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
}
