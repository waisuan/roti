package helpers

import dao.UserDao
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object TestDatabase {
    fun init() {
        Database.connect(
            "jdbc:postgresql://localhost/roti_test",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "password"
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UserDao)
        }
    }

    fun purge() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(UserDao)
        }
    }
}