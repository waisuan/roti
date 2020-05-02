
import controllers.UserController
import dao.UserDao
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Validator

fun main() {
    init()
    val app = Javalin.create().start(7000)

    app.routes {
        path("users") {
            get(UserController::getAllUsers)
            post(UserController::createUser)
            path(":username") {
                post(UserController::loginUser)
            }
        }
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
        SchemaUtils.create(UserDao)
    }
}
