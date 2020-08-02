@file:Suppress("unused")

package net.nprod.konnector.crossref

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DateBlock(
    @Json(name = "date-time") val datetime: String?,
    @Json(name = "date-parts") val dateParts: List<List<Int>>?,
    val timestamp: Long?
    // Missing date-parts
    // Missing timestamp
)

@JsonClass(generateAdapter = true)
data class Funder(
    val DOI: String?,
    val name: String?,
    @Json(name = "doi-asserted-by") val doiAssertedBy: String?,
    val award: List<String>?
)

@JsonClass(generateAdapter = true)
data class Domain(
    val domain: List<String>?,
    @Json(name = "crossmark-restriction") val crossmarkRestriction: Boolean?
)

data class Affiliation(
    val name: String?
)

@JsonClass(generateAdapter = true)
data class Author(
    val ORCID: String?,
    @Json(name = "authenticated-orcid") val authenticatedOrcid: Boolean?,
    val given: String?,
    val family: String?,
    val sequence: String?,
    val affiliation: List<Affiliation>?
)

@JsonClass(generateAdapter = true)
data class Reference(
    val key: String?,
    val author: String?,
    val volume: String?,
    @Json(name = "first-page") val firstPage: String?,
    val year: String?,
    @Json(name = "journal-title") val journalTitle: String?,
    val doi: String?,
    @Json(name = "doi-asserted-by") val doiAssertedBy: String?
)

@JsonClass(generateAdapter = true)
data class Link(
    val URL: String,
    @Json(name = "content-type") val contentType: String?,
    @Json(name = "content-version") val contentVersion: String?,
    @Json(name = "intended-application") val intendedApplication: String?
)

@JsonClass(generateAdapter = true)
data class Issue(
    @Json(name = "published-print") val publishedPrint: DateBlock?,
    val issue: String?
)

data class Relation(
    val cities: List<String>?
)

data class ISSN(
    val value: String?,
    val type: String?
)

data class Explanation(
    val URL: String?
)

data class Group(
    val name: String?,
    val label: String?
)

data class Assertion(
    val value: String?,
    val name: String?,
    val explanation: Explanation?,
    val group: Group?
)

@JsonClass(generateAdapter = true)
data class SingleWork(
    var position: Int? = 0,
    val indexed: DateBlock?,
    @Json(name = "reference-count") val referenceCount: Int?,
    val publisher: String?,
    val issue: String?,
    @Json(name = "funder") val funders: List<Funder>?,
    @Json(name = "content-domain") val contentDomain: Domain?,
    @Json(name = "short-container-title") val shortContainerTitle: List<String>?,
    val abstract: String?,
    val DOI: String,
    val type: String?,
    val created: DateBlock?,
    val page: String?,
    @Json(name = "update-policy") val updatePolicy: String?,
    val source: String?,
    @Json(name = "is-referenced-by-count") val isReferencedByCount: Int?,
    val title: List<String>?,
    val firstTitle: String? = title?.first(),
    val prefix: String?,
    val volume: String?,
    val author: List<Author>?,
    val member: String?,
    @Json(name = "published-online") val publishedOnline: DateBlock?,
    @Json(name = "published-print") val publishedPrint: DateBlock?,
    val reference: List<Reference>?,
    @Json(name = "container-title") val containerTitle: List<String>?,
    val firstContainerTitle: String? = containerTitle?.first(),
    @Json(name = "original-title") val originalTitle: List<String>?,
    val language: String?,
    val link: List<Link>?,
    val deposited: DateBlock?,
    val score: Double?,
    val subtitle: List<String>?,
    @Json(name = "short-title") val shortTitle: List<String>?,
    val issued: DateBlock?,
    @Json(name = "references-count") val referencesCount: Int?,
    @Json(name = "journal-issue") val journalIssue: Issue?,
    val URL: String?,
    val relation: Relation?,
    val ISSN: List<String>?,
    @Json(name = "issn-type") val issnType: List<ISSN>?,
    val assertion: List<Assertion>?
)

@JsonClass(generateAdapter = true)
data class WorkResponse(
    val status: String,
    @Json(name = "message-type") val messageType: String? = null,
    @Json(name = "message-version") val messageVersion: String? = null,
    val message: SingleWork? = null
)

class Facets

data class WorkList(
    val facets: Facets? = null,
    val items: List<SingleWork>? = null
    )

@JsonClass(generateAdapter = true)
data class WorksResponse(
    val status: String,
    @Json(name = "message-type") val messageType: String? = null,
    @Json(name = "message-version") val messageVersion: String? = null,
    val message: WorkList? = null
)