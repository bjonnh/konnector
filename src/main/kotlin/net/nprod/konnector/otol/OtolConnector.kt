/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class About(
    val weburl: String,
    val author: String,
    val name: String,
    val source: String,
    val version: String
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class ExtendedTaxonDescriptor(
    val ott_id: Int,
    val name: String,
    val rank: String,
    val tax_sources: List<String>,
    val unique_name: String,
    val flags: List<String>,
    val synonyms: List<String>,
    val is_suppressed: Boolean
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class TaxonInfo(
    val ott_id: Int,
    val name: String,
    val rank: String,
    val tax_sources: List<String>,
    val unique_name: String,
    val flags: List<String>,
    val synonyms: List<String>,
    val is_suppressed: Boolean,
    val lineage: List<ExtendedTaxonDescriptor>? = null,
    val children: List<ExtendedTaxonDescriptor>? = null,
    val terminal_descendants: List<Long>? = null
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class TaxInfoQuery(
    val ott_id: Int? = null,
    val source_id: String? = null,
    val include_children: Boolean,
    val include_lineage: Boolean,
    val include_terminal_descendants: Boolean
)

/**
 * Connects against OTOL
 *
 * This is currently incomplete, we only have about and taxon_info
 *
 * Following https://github.com/OpenTreeOfLife/germinator/wiki/Taxonomy-API-v3#subtree_taxonomy
 */
@ExperimentalTime
@KtorExperimentalAPI
class OtolConnector constructor(private val api: OtolAPI) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    val taxonomy = Taxonomy()

    inner class Taxonomy {
        /**
         * Get the version of the endpoint
         */
        fun about(): About {
            val output = api.call(
                api.apiURL + "taxonomy/about",
                post = true
            )
            return json.decodeFromString(
                About.serializer(),
                output
            )
        }

        /**
         * Get information on a taxon
         */
        fun taxonInfo(
            ottId: Int? = null,
            sourceId: String? = null,
            includeChildren: Boolean = false,
            includeLineage: Boolean = false,
            includeTerminalDescendants: Boolean = false
        ): TaxonInfo {
            require((ottId == null && sourceId != null) || (ottId != null && sourceId == null)) {
                "At least ottId or sourceId must be given (and not both)"
            }
            if (sourceId != null) {
                require(listOf("ncbi", "gbif", "worms", "if", "irmng").contains(sourceId.split(":")[0])) {
                    "Source id must be of the form db:id  where db is one of ncbi, gbif, worms, if, irmng"
                }
            }
            val output = api.call(
                api.apiURL + "taxonomy/taxon_info",
                post = true,
                body = json.encodeToString(
                    TaxInfoQuery.serializer(),
                    TaxInfoQuery(
                        ottId,
                        sourceId,
                        includeChildren,
                        includeLineage,
                        includeTerminalDescendants
                    )
                )
            )
            return json.decodeFromString(
                TaxonInfo.serializer(),
                output
            )
        }
    }
}
