package net.nprod.konnector.crossref

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.nprod.konnector.commons.DecodingError


@KtorExperimentalAPI
class CrossRefConnector(private val API: CrossRefAPI) {
    val json = Json { ignoreUnknownKeys = true}


    fun workFromDOIAsJson(doi: String): String = json.encodeToString<WorkResponse>(workFromDOI(doi))

    fun workFromDOIAsJsonString(doi: String): String = workFromDOIAsJson(doi)

    fun worksAsJson(title: String? = null, containerTitle: String? = null, query: String? = null): String =
        json.encodeToString<WorksResponse>(worksIO(title, containerTitle, query))

    fun worksAsJsonString(title: String? = null, containerTitle: String? = null, query: String? = null): String =
        worksAsJson(title, containerTitle, query)

    fun workFromDOI(doi: String): WorkResponse {
        val output: String = API.call(API.apiURL + "/works/$doi")
        return json.decodeFromString<WorkResponse>(output)
    }

    fun works(
        title: String? = null,
        containerTitle: String? = null, query: String? = null
    ): WorksResponse =
        worksIO(title, containerTitle, query)

    fun worksIO(
        title: String? = null,
        containerTitle: String? = null, query: String? = null
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

        val output = API.call(
            API.apiURL + "/works",
            parameters
        )

        try {
            val obj = json.decodeFromString<WorksResponse>(output)
            return obj
        } catch (e: java.io.EOFException) {
            throw DecodingError
        }
    }
}

