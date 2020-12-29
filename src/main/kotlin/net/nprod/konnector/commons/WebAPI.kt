/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */


package net.nprod.konnector.commons

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KLogger
import org.slf4j.Logger

/**
 * Any kind of WebAPI based on a Ktor HTTP Client
 */
@KtorExperimentalAPI
interface WebAPI {
    /**
     * A logger
     */
    val log: Logger

    /**
     * Delay in ms between each request
     */
    var delayTime: Long

    /**
     * How long did the last query took
     */
    var lastQueryTime: Long

    /**
     * The httpClient (this should maybe be let to the implementation?)
     */
    var httpClient: HttpClient

    /**
     * Calculates the necessary delay in milliseconds by using the last time a query was made and the
     * necessary delayTime
     */
    fun calcDelay(): Long {
        val lastQueryDelay = System.currentTimeMillis() - lastQueryTime
        return (delayTime - lastQueryDelay).coerceAtLeast(0)
    }

    /**
     * Updates the last query time
     */
    fun updateLastQueryTime() {
        lastQueryTime = System.currentTimeMillis()
    }

    /**
     * Update the delay according to the response from a call, it allows you to read HTTP headers and update
     * the delay accordingly.
     */
    fun delayUpdate(call: HttpResponse): Unit = updateLastQueryTime()

    /**
     * call the API
     *
     * @param url The URL to query
     * @param params a map of the HTTP request parameters that will be sent by GET (so don't make them too big)
     * @param retries how many times the query is going to retry
     */
    fun call(url: String, parameters: MutableMap<String, String>? = null, retries: Int = 3): String {
        log.debug("Connecting to $url")

        return try {
            val call = runBlocking {
                delay(calcDelay())
                val response = httpClient.request<HttpResponse>(url) {
                    method = HttpMethod.Get
                    parameters?.forEach { (k, v) -> parameter(k, v) }
                }
                delayUpdate(response)
                when (response.status.value) {
                    200 -> response.readText()
                    404 -> throw NonExistentReference
                    400 -> throw BadRequestError(response.readText())
                    429 -> {
                        delay(2_000)
                        throw TooManyRequests
                    } // We block for 2s in case of rate limiting trigger
                    else -> throw UnManagedReturnCode(response.status.value)
                }
            }
            call
        } catch (e: KnownError) {
            if (retries > 0) return call(url, parameters, retries - 1)
            throw e
        }
    }

    /**
     * Obtain a new HTTP client
     *
     * @param module Unused kept for backward compatibility
     */
    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "This parameter was not used and is going to be removed",
        replaceWith = ReplaceWith(expression = "newClient()")
    )
    fun newClient(module: String): HttpClient = newClient()

    /**
     * Obtain a new HTTP client
     *
     * @param module Unused kept for backward compatibility
     */

    fun newClient(): HttpClient = HttpClient(CIO) {
        expectSuccess = false
        engine {
            threadsCount = 4
            with(endpoint) {
                connectAttempts = 5
            }
        }
    }
}
