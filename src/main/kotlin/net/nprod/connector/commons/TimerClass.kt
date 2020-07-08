package net.nprod.connector.commons

import mu.KotlinLogging

class TimerClass {
    var start = System.currentTimeMillis()
    internal val logger = KotlinLogging.logger {}

    fun timeSinceStart(prefix: String = "") {
        logger.info("$prefix ${System.currentTimeMillis() - start}")
    }

    @Suppress("unused")
    fun resetTime() {
        start = System.currentTimeMillis()
    }
}