package net.nprod.connector.pubmed

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.nprod.connector.commons.BadRequestError


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

@Suppress("Duplicates")
fun EntrezConnector.efetch(
    ids: List<Long>? = null, retmax: Int? = null, retstart: Int? = null,
    webenv: String? = null, querykey: Int? = null, idlist: Boolean = false
): EFetch {
    if ((!webenv.isNullOrEmpty()) and (!ids.isNullOrEmpty())) throw Error("Cannot work with ids and WebEnv")
    // TODO Find why it was there in the first place
    //if ((webenv == null) and (querykey != null)) throw Error("querykey only works with a webenv gave querykey=${querykey}")

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

    val call = call(eFetchapiURL, parameters)

    return EFetch(result=call)
}

/**
 * Continues a query
 * If the query has a webenv, we continue to use it
 *
 * @param query The query to be continued
 * @param retmax The maximum number of results to return for that query (can be null, in that case it takes
 * the value from query).
 * @param retstart The starting offset for that query (can be null, in that case it takes the value from query).
 */

fun EntrezConnector.efetchNext(query: EFetch, retmax: Int? = null, retstart: Int? = null): EFetch {
    return efetch(
        null, retmax = retmax ?: query.retmax,
        retstart = retstart ?: (query.retstart ?: 0) + (query.retmax ?: 10),
        webenv = query.webenv,
        querykey = query.querykey
    )
}
