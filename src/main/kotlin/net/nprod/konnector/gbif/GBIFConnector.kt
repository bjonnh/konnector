package net.nprod.konnector.gbif

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Occurence(
    val key: String,
    val decimalLongitude: Double? = null,
    val decimalLatitude: Double? = null,
    val acceptedScientificName: String,
)

@Serializable
data class OccurenceSearchResponse (
    val offset: Int,
    val limit: Int,
    val endOfRecords: Boolean,
    val count: Long,
    val results: List<Occurence>
)

@KtorExperimentalAPI
class GBIFConnector(private val API: GBIFAPI) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun occurenceOfTaxon(taxonKey: String,
    limit: Int = 20, offset: Int = 0,
    basisOfRecord: String? = null): OccurenceSearchResponse {
        val parameters = mutableMapOf(
            "taxonKey" to taxonKey,
            "limit" to "$limit",
            "offset" to "$offset"
        )

        basisOfRecord?.let {
            parameters["basisOfRecord"] = it
        }

        val output = API.call(
            API.apiURL + "occurrence/search",
            parameters
        )

        return json.decodeFromString(
            OccurenceSearchResponse.serializer(),
            output
        )
    }
}