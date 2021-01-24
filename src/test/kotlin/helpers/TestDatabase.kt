package helpers

import configs.Config
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable

object TestDatabase {
    fun init() {
        val dbUrl = Config.testDbUrl
        val dbUser = Config.testDbUser
        val dbPwd = Config.testDbPwd

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

    fun purge() {
        transaction {
            TransactionManager.current().exec("TRUNCATE TABLE ${UserTable.tableName};")
            TransactionManager.current().exec("TRUNCATE TABLE ${MachineTable.tableName} CASCADE;")
            TransactionManager.current().exec("TRUNCATE TABLE ${MaintenanceTable.tableName};")
        }
    }
}
