/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */


package net.nprod.konnector.pubmed

import com.ctc.wstx.stax.WstxInputFactory
import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.CachingXMLResolver
import net.nprod.konnector.commons.allText
import net.nprod.konnector.commons.attributes
import net.nprod.konnector.commons.contentAsXML
import net.nprod.konnector.commons.document
import net.nprod.konnector.commons.element
import net.nprod.konnector.commons.tagText
import net.nprod.konnector.pubmed.models.PubmedArticle
import org.codehaus.stax2.XMLInputFactory2
import java.io.InputStream
import javax.xml.stream.XMLInputFactory

/**
 * Parse a EFetch XML publication list and create simpler PubmedArticle objects.
 */
@KtorExperimentalAPI
class EFetchPubmedParser {
    private val factory: XMLInputFactory2

    init {
        factory = WstxInputFactory()
        factory.configureForSpeed()
        factory.xmlResolver = CachingXMLResolver()
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false)
    }

    @Suppress("unused")
    fun parsePubmedArticlesIn(string: String): List<PubmedArticle?> = parsePubmedArticlesIn(string.byteInputStream())

    fun parsePubmedArticlesIn(stream: InputStream): List<PubmedArticle?> {

        val reader = factory.createXMLStreamReader(stream)

        val articleList = reader.document {
            if (this.hasText()) this.elementText
            element("PubmedArticle") {
                PubmedArticle().apply {
                    element("MedlineCitation", "ArticleIdList") {
                        when (it) {
                            "ArticleIdList" -> element("ArticleId") {

                                attributes["IdType"]?.let { idType ->
                                    if (idType == "doi") {
                                        DOI = allText("ArticleId")
                                    }
                                }
                            }
                            "MedlineCitation" -> element("PMID", "Article") {
                                // When we have an erratum, there are two PMID elements at different depths
                                // this also shows that parsing xml by streaming like that isn't perfect
                                if (it == "PMID") { if (pmid == null) pmid = allText("PMID") } else if (it == "Article") {
                                    element("Journal", "Abstract", "ArticleTitle") {
                                        when (it) {
                                            "Journal" -> {
                                                element("Title", "JournalIssue") {
                                                    when (it) {
                                                        "Title" -> journalTitle = allText("Title")
                                                        "JournalIssue" ->
                                                            element("PubDate", "Volume", "Issue") {
                                                                when (it) {
                                                                    "PubDate" -> year = tagText("Year")
                                                                    "Volume" -> volume = allText("Volume")
                                                                    "Issue" -> issue = allText("Issue")
                                                                }
                                                            }
                                                        else -> {
                                                        }
                                                    }
                                                }
                                            }
                                            "Abstract" -> abstract = allText("Abstract")
                                            "ArticleTitle" -> articleTitle = allText("ArticleTitle")
                                            else -> {
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        reader.close()
        return articleList
    }

    /**
     * Parse a stream containing multiple PubmedArticles and returns a list of XML strings for each individual one
     *
     * @param stream  The input stream
     */
    fun parsePubmedArticlesAsRaw(stream: InputStream): List<String> {
        val reader = factory.createXMLStreamReader(stream)
        return reader.document {
            if (hasText()) elementText
            element("PubmedArticle") {
                contentAsXML("PubmedArticle")
            }
        }
    }
}
