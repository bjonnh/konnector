/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */


package net.nprod.konnector.gbif

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import mu.KLogger
import mu.KotlinLogging
import org.slf4j.Logger

/**
 * Connect to the official GBIF API.
 */
@KtorExperimentalAPI
class OfficialGBIFAPI : GBIFAPI {
    override val log: Logger = KotlinLogging.logger(this::class.java.name)
    override var httpClient: HttpClient = newClient()
    override var delayTime: Long = 20L // Delay in ms between each request
    override var lastQueryTime: Long = System.currentTimeMillis()
    override var apiURL: String = "https://api.gbif.org/v1/"

    /**
     * Updates the necessary delay from the HTTP headers received
     *
     * @param limit Is the number of elements
     * @param interval Is the period in the format <number>s (currently we have only seen 1s)
     *
     */
    private fun updateDelayFromHeaderData(limit: String? = "50", interval: String? = "1s") {
        val intervalInt = interval?.filter { it != 's' }?.toIntOrNull()
        val limitInt = limit?.toLongOrNull()
        if ((intervalInt != null) && (limitInt != null))
            delayTime = 1_000 * intervalInt / limitInt
    }

    /**
     * We use a different approach in that module as the API can tell us to slow down (and they do)
     */
    override fun delayUpdate(call: HttpResponse) {

        updateDelayFromHeaderData(
            call.headers["X-Rate-Limit-Limit"],
            call.headers["X-Rate-Limit-Interval"]
        )
        updateLastQueryTime()
    }
}
