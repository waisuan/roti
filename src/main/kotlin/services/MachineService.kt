package services

import dao.MachineDao
import exceptions.BadOperationException
import exceptions.RecordAlreadyExistsException
import exceptions.RecordNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import models.Machine
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MachineTable

object MachineService {
    fun getAllMachines(limit: Int = 0, offset: Long = 0, sortFilter: String = "updatedAt", sortOrder: String = "desc"): List<Machine> {
        val requestKey = "${limit}_${offset}_${sortFilter}_$sortOrder"
        return if (CacheService.isMachinesCached(requestKey)) {
            CacheService.getMachines(requestKey)
        } else {
            val machines = transaction {
                MachineDao.all()
                    .let { it.limit(limit, offset).takeIf { limit > 0 } ?: it }
                    .orderBy(MachineTable.columns.first { it.name == sortFilter } to SortOrder.valueOf(sortOrder.toUpperCase()))
                    .map { it.toModel() }
            }
            CacheService.setMachines(requestKey, machines)
            machines
        }
    }

    fun createMachine(newMachine: Machine) {
        if (!newMachine.isValid())
            throw BadOperationException(Machine::class.java.simpleName)

        transaction {
            if (MachineDao.find { MachineTable.serialNumber eq newMachine.serialNumber!! }.firstOrNull() != null)
                throw RecordAlreadyExistsException()

            MachineDao.new {
                serialNumber = newMachine.serialNumber!!
                customer = newMachine.customer
                state = newMachine.state
                accountType = newMachine.accountType
                model = newMachine.model
                status = newMachine.status
                brand = newMachine.brand
                district = newMachine.district
                personInCharge = newMachine.personInCharge
                reportedBy = newMachine.reportedBy
                additionalNotes = newMachine.additionalNotes
                attachment = newMachine.attachment
                tncDate = newMachine.tncDate
                ppmDate = newMachine.ppmDate
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
        }
        CacheService.purgeCachedMachines()
    }

    fun updateMachine(serialNumber: String, updatedMachine: Machine): Machine {
        val machine = transaction {
            val machine = MachineDao.find { MachineTable.serialNumber eq serialNumber }.firstOrNull()
            if (machine != null) {
                machine.customer = updatedMachine.customer
                machine.state = updatedMachine.state
                machine.accountType = updatedMachine.accountType
                machine.model = updatedMachine.model
                machine.status = updatedMachine.status
                machine.brand = updatedMachine.brand
                machine.district = updatedMachine.district
                machine.personInCharge = updatedMachine.personInCharge
                machine.reportedBy = updatedMachine.reportedBy
                machine.additionalNotes = updatedMachine.additionalNotes
                machine.attachment = updatedMachine.attachment
                machine.tncDate = updatedMachine.tncDate
                machine.ppmDate = updatedMachine.ppmDate
                machine.updatedAt = LocalDateTime.now()
                machine.version = updatedMachine.version + 1
            } else {
                throw RecordNotFoundException()
            }
            machine.toModel()
        }
        CacheService.purgeCachedMachines()
        return machine
    }

    fun deleteMachine(serialNumber: String) {
        transaction {
            val machine = MachineDao.find { MachineTable.serialNumber eq serialNumber }.firstOrNull()
            if (machine != null) {
                machine.delete()
            } else {
                throw RecordNotFoundException()
            }
        }
        CacheService.purgeCachedMachines()
    }

    fun searchMachine(keyword: String, limit: Int = 0, offset: Long = 0, sortFilter: String = "updatedAt", sortOrder: String = "desc"): List<Machine> {
        return transaction {
            MachineDao.find { MachineTable.document.lowerCase() like "%${keyword.toLowerCase()}%" }
                .let { it.limit(limit, offset).takeIf { limit > 0 } ?: it }
                .orderBy(MachineTable.columns.first { it.name == sortFilter } to SortOrder.valueOf(sortOrder.toUpperCase()))
                .map {
                    it.toModel()
                }
        }
    }

    fun getNumberOfMachines(keyword: String? = null): Long {
        return transaction {
            if (keyword == null) {
                val requestKey = "count"
                if (CacheService.isMachinesCached(requestKey)) {
                    CacheService.getMachineCount(requestKey)!!
                } else {
                    val count = MachineDao.all().count()
                    CacheService.setMachineCount(requestKey, count)
                    count
                }
            } else {
                MachineDao.find { MachineTable.document.lowerCase() like "%${keyword.toLowerCase()}%" }.count()
            }
        }
    }

    fun getNumOfPpmDueMachines(date: LocalDate = LocalDate.now()): Int {
        return transaction {
            TransactionManager.current().exec("""
                select
                    count(*) AS count
                from
                    machines m
                where
                    (m."ppmDate" - '$date'::date) between -14 and 14;
            """.trimIndent()) {
                it.next()
                it.getInt("count")
            }
        } ?: 0
    }

    fun getPpmDueMachines(date: LocalDate = LocalDate.now()): List<Machine> {
        return transaction {
            val machines = mutableListOf<Machine>()
            TransactionManager.current().exec("""
                ${ppmDueMachinesSubQuery("almost_due", date, 1, 14)}
                union
                ${ppmDueMachinesSubQuery("due", date, 0, 0)}
                union
                ${ppmDueMachinesSubQuery("overdue", date, -14, -1)}
            """.trimIndent()) {
                while (it.next()) {
                    machines.add(
                        Machine(
                            it.getString("serialNumber"),
                            it.getString("customer"),
                            it.getString("state"),
                            it.getString("accountType"),
                            it.getString("model"),
                            it.getString("status"),
                            it.getString("brand"),
                            it.getString("district"),
                            it.getString("personInCharge"),
                            it.getString("reportedBy"),
                            it.getString("additionalNotes"),
                            it.getString("attachment"),
                            it.getString("ppmStatus"),
                            it.getDate("tncDate")?.toLocalDate(),
                            it.getDate("ppmDate")?.toLocalDate(),
                            it.getTimestamp("createdAt")?.toLocalDateTime(),
                            it.getTimestamp("updatedAt")?.toLocalDateTime()
                        )
                    )
                }
            }
            machines
        }
    }

    private fun ppmDueMachinesSubQuery(ppmStatus: String, date: LocalDate, from: Int, to: Int): String {
        return """
            select
                '$ppmStatus' as ppmStatus,
                m.*
            from
                machines m
            where
                (m."ppmDate" - '$date'::date) between $from and $to
        """.trimIndent()
    }
}
