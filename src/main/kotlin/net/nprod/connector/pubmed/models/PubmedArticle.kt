package net.nprod.connector.pubmed.models

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class PubmedArticle(
    var pmid: String? = null,
    var journalTitle: String? = null,
    var abstract: String? = null,
    var articleTitle: String? = null,
    var year: String? = null,
    var DOI: String? = null
) {
    @UnstableDefault
    fun asString(): String = Json.stringify(serializer(),this)
}