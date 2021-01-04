/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import com.google.protobuf.Field
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class Version(
    val version: String,
    val build: String
)

@Serializable
data class DataSource(
    val id: Int,
    val uuid: String? = null,
    val title: String,
    val titleShort: String,
    val description: String? = null,
    val homeURL: String? = null,
    val isOutlinkReady: Boolean? = null,
    val curation: String,
    val recordCount: Long,
    val updatedAt: String
)

@Serializable
data class VerificationQuery(
    val nameStrings: List<String>,
    val preferredSources: List<Int>,
    val withVernaculars: Boolean
)

@Serializable
data class Result(
    val dataSourceId: Int,
    val dataSourceTitleShort: String?,
    val curation: String,
    val recordId: String,
    val localId: String,
    val outlink: String? = null,
    val entryDate: String,
    val matchedName: String? = null,
    val matchedCardinality: String? = null,
    val matchedCanonicalSimple: String? = null,
    val matchedCanonicalFull: String? = null,
    val currentRecordId: String? = null,
    val currentName: String? = null,
    val currentCardinality: String? = null,
    val currentCanonicalSimple: String? = null,
    val currentCanonicalFull: String? = null,
    val isSynonym: Boolean? = null,
    val classificationPath: String? = null,
    val classificationRanks: String? = null,
    val editDistance: Int? = null,
    val stemEditDistance: Int? = null,
    val matchType: String? = null
)

@Serializable
data class Verification(
    val inputId: String,
    val input: String,
    val matchType: String,
    val bestResult: Result? = null,
    val preferredResults: List<Result>? = null,
    val dataSourcesNum: Int,
    val curation: String
)

/**
 * Connects against GlobalNames verify
 *
 * It is not handling the in query name verification, but that shouldn't change anything for the end user
 *
 * There is some work to do so we get the "possible values" documented in the API doc handled properly:
 * https://app.swaggerhub.com/apis-docs/dimus/gnames/1.0.0#/Verification
 */
@ExperimentalTime
@KtorExperimentalAPI
class GlobalNamesVerifyConnector constructor(private val api: GlobalNamesVerifyAPI) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Issues a ping request
     *
     * @return true if received pong
     */
    fun ping(): Boolean {
        val output = api.call(
            api.apiURL + "ping"
        )
        return output == "pong"
    }

    /**
     * Get the version of the endpoint
     */
    fun version(): Version {
        val output = api.call(
            api.apiURL + "version"
        )
        return json.decodeFromString(
            Version.serializer(),
            output
        )
    }

    /**
     * Get a list of data sources
     */

    fun dataSources(): List<DataSource> {
        val output = api.call(
            api.apiURL + "data_sources"
        )
        return json.decodeFromString(
            ListSerializer(DataSource.serializer()),
            output
        )
    }

    /**
     * Get the info of a specific source
     */

    fun dataSource(id: Int): DataSource {
        if (id <= 0) throw IllegalArgumentException("ID of data source must be greater than 0")
        val output = api.call(
            api.apiURL + "data_sources/$id"
        )
        return json.decodeFromString(
            DataSource.serializer(),
            output
        )
    }

    /**
     * Get the info about one or many organisms
     */
    fun verifications(query: VerificationQuery): List<Verification> {
        val output = api.call(
            api.apiURL + "verifications",
            post = true,
            body = json.encodeToString(query)
        )
        return json.decodeFromString(
            ListSerializer(Verification.serializer()),
            output
        )
    }

/*    fun taxonkeyByName(name: String): TaxonSearchResponse {
        val output = api.call(
            api.apiURL + "species/match",
            mutableMapOf("name" to name)
        )

        return json.decodeFromString(
            TaxonSearchResponse.serializer(),
            output
        )
    }

    fun occurenceOfTaxon(
        q: String? = null,
        taxonKey: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        basisOfRecord: String? = null
    ): OccurenceSearchResponse {
        val parameters = mutableMapOf(
            "limit" to "$limit",
            "offset" to "$offset"
        )

        taxonKey?.let {
            parameters["taxonKey"] = it
        }

        basisOfRecord?.let {
            parameters["basisOfRecord"] = it
        }

        q?.let {
            parameters["q"] = it
        }

        val output = api.call(
            api.apiURL + "occurrence/search",
            parameters
        )

        return json.decodeFromString(
            OccurenceSearchResponse.serializer(),
            output
        )
    }*/
}
