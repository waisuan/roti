
import controllers.AuthController
import controllers.MachineController
import controllers.MaintenanceController
import controllers.UserController
import dao.UserDao
import exceptions.RecordNotFoundException
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

fun main() {
    init()
    val app = Javalin.create {
        it.accessManager(AuthController::accessManager)
    }.apply {
        ws("/websocket") { ws ->
            ws.onConnect { }
            ws.onMessage { }
        }
    }.start(7000)

    app.routes {
        path("users") {
            get(UserController::getAllUsers)
            post(UserController::createUser)
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
                }
            }
        }
    }

    app.exception(RecordNotFoundException::class.java) { e, ctx ->
        ctx.result(e.message!!)
        ctx.status(404)
    }
}

fun init() {
    Database.connect(
        "jdbc:postgresql://localhost/roti",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(
            UserDao,
            MachineTable,
            MaintenanceTable
        )
    }
}
