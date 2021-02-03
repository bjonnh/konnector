/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.pubmed.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Author(
    val lastName: String? = null,
    val foreName: String? = null,
    val initials: String? = null,
    val affiliation: String? = null
)

@Serializable
data class PubmedArticle(
    var pmid: String? = null,
    var journalTitle: String? = null,
    var abstract: String? = null,
    var articleTitle: String? = null,
    var year: String? = null,
    @SerialName("DOI") var doi: String? = null,
    var volume: String? = null,
    var authors: MutableList<Author> = mutableListOf(),
    var issue: String? = null
) {
    @Suppress("unused")
    fun asString(): String = Json.encodeToString(serializer(), this)
}
