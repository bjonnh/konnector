/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.crossref

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.nprod.konnector.commons.DecodingError
import java.net.SocketTimeoutException
import kotlin.time.ExperimentalTime

/**
 * Connect to a real CrossREF compliant endpoint
 */
@KtorExperimentalAPI
@ExperimentalTime
class CrossRefConnector constructor(private val api: CrossRefAPI) {
    private val json = Json { ignoreUnknownKeys = true }

    fun workFromDOIAsJson(doi: String): String = json.encodeToString<WorkResponse>(workFromDOI(doi))

    fun workFromDOIAsJsonString(doi: String): String = workFromDOIAsJson(doi)

    fun worksAsJson(title: String? = null, containerTitle: String? = null, query: String? = null): String =
        json.encodeToString<WorksResponse>(worksIO(title, containerTitle, query))

    fun worksAsJsonString(title: String? = null, containerTitle: String? = null, query: String? = null): String =
        worksAsJson(title, containerTitle, query)

    fun workFromDOI(doi: String): WorkResponse {
        val output: String = api.call(api.apiURL + "/works/$doi")
        return json.decodeFromString<WorkResponse>(output)
    }

    fun works(
        title: String? = null,
        containerTitle: String? = null,
        query: String? = null
    ): WorksResponse =
        worksIO(title, containerTitle, query)

    fun worksIO(
        title: String? = null,
        containerTitle: String? = null,
        query: String? = null
    ): WorksResponse {
        val parameters = mutableMapOf(
            "select" to "DOI,title,container-title,published-print,published-online,author,abstract,volume",
            "sort" to "relevance",
            "order" to "desc"
        )
        if (query != null)
            parameters["query"] = query
        if (title != null)
            parameters["query.bibliographic"] = title
        if (containerTitle != null)
            parameters["query.container-title"] = containerTitle

        val output = try {
            api.call(
                api.apiURL + "/works",
                parameters
            )
        } catch (e: io.ktor.network.sockets.SocketTimeoutException) {
            throw SocketTimeoutException()
        }

        return try {
            json.decodeFromString(output)
        } catch (e: java.io.EOFException) {
            throw DecodingError("End of file received before the end of the JSON.")
        }
    }
}
