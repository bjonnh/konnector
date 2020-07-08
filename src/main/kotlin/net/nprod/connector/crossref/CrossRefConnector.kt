package net.nprod.connector.crossref

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.util.KtorExperimentalAPI
import net.nprod.connector.commons.DecodingError


@KtorExperimentalAPI
class CrossRefConnector(private val API: CrossRefAPI) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val workAdapter: JsonAdapter<WorkResponse>
    private val worksAdapter: JsonAdapter<WorksResponse>

    init {
        workAdapter = moshi.adapter(WorkResponse::class.java)
        worksAdapter = moshi.adapter(WorksResponse::class.java)
    }

    fun workFromDOIAsJson(doi: String): String {
        return workAdapter.toJson(workFromDOI(doi))
    }

    fun workFromDOIAsJsonString(doi: String): String = workFromDOIAsJson(doi)

    fun worksAsJson(title: String? = null, containerTitle: String? = null, query: String? = null): String {
        return worksAdapter.toJson(worksIO(title, containerTitle, query))
    }

    fun worksAsJsonString(title: String? = null, containerTitle: String? = null, query: String? = null): String =
        worksAsJson(title, containerTitle, query)

    fun workFromDOI(doi: String): WorkResponse {
        val output = API.call(API.apiURL + "/works/$doi")
        val obj = workAdapter.fromJson(output)
        return obj ?: throw DecodingError
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
            val obj = worksAdapter.fromJson(output)
            return obj ?: throw DecodingError
        } catch (e: java.io.EOFException) {
            throw DecodingError
        }
    }
}

