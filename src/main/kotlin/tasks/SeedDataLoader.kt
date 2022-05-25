package tasks

import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random
import models.Machine
import models.Maintenance
import picocli.CommandLine
import services.MachineService
import services.MaintenanceService
import utils.logger

@CommandLine.Command(name = "seed_data_loader", description = ["load seed data into target DB"])
class SeedDataLoader : Task {
    @CommandLine.Option(names = ["-t", "--total"], required = false)
    var total: Int = 100

    @CommandLine.Option(names = ["-x", "--type"], required = true)
    var type: String = "machine"

    override fun call(): Int {
        if (type == "machine")
            seededMachineData()
        else if (type == "maintenance")
            seededMaintenanceData()
        else
            logger().warn("Skipping due to unrecognized type: $type")

        return 0
    }

    private fun seededMachineData() {
        var success = 0; var failed = 0
        for (i in 1..total) {
            val m = Machine(
                serialNumber = UUID.randomUUID().toString().takeLast(12),
                customer = randomString(),
                state = randomString(),
                accountType = randomString(),
                model = randomString(),
                status = randomString(),
                brand = randomString(),
                district = randomString(),
                personInCharge = randomString(),
                reportedBy = randomString(),
                additionalNotes = randomString(80),
                tncDate = LocalDate.now().minusDays(Random.nextLong(-365, 365)),
                ppmDate = LocalDate.now().minusDays(Random.nextLong(-365, 365))
            )

            try {
                logger().info(">>> Inserting: $m")
                MachineService.createMachine(m)
                success++
            } catch (e: Exception) {
                logger().error(">>> Skipping because of: ${e.message}")
                failed++
            }
        }

        logger().info(">>> Total no. of data loaded: $success | Total no. of failures: $failed")
    }

    private fun seededMaintenanceData() {
        var success = 0; var failed = 0
        MachineService.getAllMachines(limit = 5).forEach { machine ->
            for (i in 1..total) {
                val m = Maintenance(
                    workOrderNumber = UUID.randomUUID().toString().takeLast(8),
                    workOrderDate = LocalDate.now().minusDays(Random.nextLong(-365, 365)),
                    actionTaken = randomString(80),
                    reportedBy = randomString(),
                    workOrderType = randomString(5)
                )

                try {
                    logger().info(">>> Inserting: $m")
                    MaintenanceService.createMaintenanceHistory(machine.serialNumber!!, m)
                    success++
                } catch (e: Exception) {
                    logger().error(">>> Skipping because of: ${e.message}")
                    failed++
                }
            }
        }

        logger().info(">>> Total no. of data loaded: $success | Total no. of failures: $failed")
    }

    private fun randomString(length: Int = 10): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
