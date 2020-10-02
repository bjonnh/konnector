package net.nprod.konnector.pubmed.models

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class PubmedArticle(
    var pmid: String? = null,
    var journalTitle: String? = null,
    var abstract: String? = null,
    var articleTitle: String? = null,
    var year: String? = null,
    var DOI: String? = null,
    var volume: String? = null,
    var issue: String? = null
) {
    @Suppress("unused")
    fun asString(): String = Json.encodeToString(serializer(),this)
}