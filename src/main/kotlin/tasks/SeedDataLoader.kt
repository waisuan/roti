package tasks

import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random
import models.Machine
import picocli.CommandLine
import services.MachineService
import utils.logger

@CommandLine.Command(name = "seed_data_loader", description = ["load seed data into target DB"])
class SeedDataLoader : Task {
    @CommandLine.Option(names = ["-t", "--total"], required = false)
    var total: Int = 100

    override fun call(): Int {
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

        return 0
    }

    private fun randomString(length: Int = 10): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
