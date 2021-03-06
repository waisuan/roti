package internal

import configs.Config
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable
import tables.MaintenanceTable
import tables.UserTable

object Database {
    fun init(enableMigrations: Boolean = true) {
        val dbUrl = Config.dbUrl
        val dbUser = Config.dbUser
        val dbPwd = Config.dbPwd

        Database.connect(
            url = dbUrl,
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPwd
        )

        if (enableMigrations) {
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
}
