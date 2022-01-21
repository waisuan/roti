package jobs

import utils.logger

object EmailJob {
    fun perform(run: () -> Unit) {
        logger().info("Starting EmailJob...")
        run()
        logger().info("EmailJob has finished...")
    }
}
