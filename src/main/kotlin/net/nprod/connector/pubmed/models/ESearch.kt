@file:Suppress("unused", "SpellCheckingInspection")

package net.nprod.connector.pubmed.models

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class Header(
    val type: String,
    val version: String
)

@Serializable
data class Translation(
    val from: String,
    val to: String
)

sealed class GenericTranslationStackElement

@Serializable
data class OperatorStackElement(
    val operator: String
): GenericTranslationStackElement()

@Serializable
data class TranslationStackElement(
    val term: String,
    val field: String,
    val count: Int,
    val explode: String
) : GenericTranslationStackElement()

@Serializable
data class Esearchresult(
    val count: Int,
    val retmax: Int? = null,
    val retstart: Int? = null,
    val querykey: Int? = null,
    val webenv: String? = null,
    var idlist: List<Int>? = null,
    var translationset: List<Translation>? = null,
    //val translationstack: List<TranslationStackElement>, // This isn't easy to do with Moshi
    var querytranslation: String? = null
)

/**
 * The storage class for any Esearch result. It currently lacks support for translationstack
 */

@Serializable
data class Esearch(
    val header: Header,
    val esearchresult: Esearchresult,
    var query: String? = null,
    @SerialName("message-type")
    val messageType: String? = null,
    @SerialName("message-version")
    val messageVersion: String? = null
) {
    /**
     * Get this object as a JSON string
     */
    @UnstableDefault
    fun asString(): String = Json.stringify(serializer(),this)

    /**
     * Reduce the size of the object, mainly used to continue queries and fetch the next entries
     */
    fun minimalize() {
        esearchresult.idlist = null
        esearchresult.translationset = null
        esearchresult.querytranslation = null
    }


    /**
     * Grab the number of citations that are can still be obtained (dynamic)
     */
    val citationsLeft: Int?
        get() = if (esearchresult.retmax != null) {
            esearchresult.count - esearchresult.retmax
        } else {
            null
        }

    /**
     * Is it a count query only (dynamic)
     */

    val countOnly: Boolean
        get() = esearchresult.querytranslation == null

    /**
     * Build an object from a JSON string
     */
    companion object {
        @OptIn(UnstableDefault::class)
        fun fromString(str: String): Esearch = try { Json.parse(serializer(), str) }
        catch (e: SerializationException) {
            throw ParsingError("Invalid JSON $e")
        }
    }
}

class ParsingError(s: String) : Throwable(s)