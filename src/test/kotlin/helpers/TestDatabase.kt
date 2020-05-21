package helpers

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable

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
            SchemaUtils.create(UserTable, MachineTable, MaintenanceTable)
        }
    }

    fun purge() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(UserTable)
            SchemaUtils.drop(MaintenanceTable)
            SchemaUtils.drop(MachineTable)
        }
    }
}
