/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.pubmed

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime

/**
 * Default page size for eFetchNext
 */
const val ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT: Int = 10

/**
 * An object to keep the EFetch status of the last request
 */
data class EFetch(
    val webenv: String? = null,
    val querykey: Int? = null,
    val retmode: String? = null,
    val rettype: String? = null,
    val retstart: Int? = null,
    val retmax: Int? = null,
    val result: String = "",
    val error: Throwable? = null
)

/**
 * Runs a query on Efetch
 *
 * @param ids a list of ids
 * @param retmax Maximum number of results to return
 * @param retstart Starting offset to recover the results
 * @param webenv The webenvironment to reuse
 * @param querykey The querykey this query will add to
 * @param idlist When true, only returns the IDs
 */

@ExperimentalTime
@KtorExperimentalAPI
@Suppress("Duplicates", "LongParameterList")
fun EntrezConnector.efetch(
    ids: List<Long>? = null,
    retmax: Int? = null,
    retstart: Int? = null,
    webenv: String? = null,
    querykey: Int? = null,
    idlist: Boolean = false
): EFetch {
    if ((!webenv.isNullOrEmpty()) and (!ids.isNullOrEmpty()))
        throw IllegalArgumentException("Cannot work with ids and WebEnv")
    // TODO Find why it was there in the first place
    // if ((webenv == null) and (querykey != null))
    //      throw Error("querykey only works with a webenv gave querykey=${querykey}")

    val parameters = defaultParameters.toMutableMap()

    if (idlist) {
        parameters["retmode"] = "text"
        parameters["rettype"] = "uilist"
    } else {
        parameters["retmode"] = "xml"
    }

    if (ids != null)
        parameters["id"] = ids.joinToString(",")

    if (retmax != null)
        parameters["retmax"] = retmax.toString()

    if (retstart != null)
        parameters["retstart"] = retstart.toString()

    if (webenv != null)
        parameters["webenv"] = webenv

    if (querykey != null)
        parameters["query_key"] = querykey.toString()
    runBlocking { delay(calcDelay()) }
    log.info("Calling URL: $eFetchapiURL")
    log.debug(" With parameters: $parameters")
    val call = call(eFetchapiURL, parameters)

    return EFetch(result = call)
}

/**
 * Continues a query
 * If the query has a webenv, we continue to use it
 *
 * @param query The query to be continued
 * @param retmax The maximum number of results to return for that query (can be null, in that case it takes
 * the value from query, which if it is null, will take the ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT).
 * @param retstart The starting offset for that query (can be null, in that case it takes the value from query).
 */

@ExperimentalTime
@KtorExperimentalAPI
fun EntrezConnector.efetchNext(query: EFetch, retmax: Int? = null, retstart: Int? = null): EFetch {
    return efetch(
        null,
        retmax = retmax ?: query.retmax ?: ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT,
        retstart = retstart ?: (query.retstart ?: 0) + (query.retmax ?: ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT),
        webenv = query.webenv,
        querykey = query.querykey
    )
}
