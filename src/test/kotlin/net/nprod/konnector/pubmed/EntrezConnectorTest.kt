package net.nprod.konnector.pubmed

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.pubmed.models.Esearch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
internal class EntrezConnectorTest {
    private val eFetchPubmedParser = EFetchPubmedParser()
    private var eSearchConn: EntrezConnector = EntrezConnector(System.getenv("NCBI_APIKEY"), 1000)

    @Test
    fun `basic search`() {
        val output = eSearchConn.esearch("Science[journal] AND curcumin")
        assert(output.esearchresult.count >= 2)
    }

    @Test
    fun `Limits of results`() {
        val output = eSearchConn.esearch("curcumin", retmax = 2)
        assert(output.citationsLeft ?: 0 > 0)
    }

    @Test
    fun `Next page of results`() {
        val output = eSearchConn.esearch("curcumin", retmax = 2)
        val nextOutput = eSearchConn.esearchNext(output)
        assert(nextOutput.citationsLeft ?: 0 > 0)
    }

    @Test
    fun `Use history server`() {
        val output = eSearchConn.esearch("curcumin", retmax = 2, usehistory = true)
        assertNotNull(output.esearchresult.querykey)
        assertNotNull(output.esearchresult.webenv)
    }

    @Test
    fun `Append using history server`() {
        val output = eSearchConn.esearch("curcumin", retmax = 2, usehistory = true)
        eSearchConn.esearchNext(output)
        assertNotNull(output.esearchresult.querykey)
        assertNotNull(output.esearchresult.webenv)
    }

    @Test
    fun `basic search count`() {
        val output = eSearchConn.esearch("Science[journal] AND curcumin", countonly = true)
        assert(output.esearchresult.count >= 2)
        assertNull(output.esearchresult.idlist)
    }

    @Test
    fun `basic Efetch test`() {
        val output = eSearchConn.efetch(listOf(17284678))
        assert(output.result != "")
    }

    @Test
    fun `Display the current time requested`() {
        val output = eSearchConn.efetch(listOf(17284678))
        println("Current delay is ${eSearchConn.delay}")
        assert(output.result != "")
    }

    @Test
    fun `Testing multi page requests`() {
        val eSearch = Esearch.fromString(
            eSearchConn.esearch(
                "sitosterol curcumin"
            ).asString()
        )

        eSearchConn.getNewIDs(
            (eSearch.esearchresult.webenv ?: ""),
            (eSearch.esearchresult.querykey ?: 1),
            retmax = 2
        )
    }

    @Test
    fun `Make sure articles with corrections match the correction id`() {
        val output = eSearchConn.efetch(listOf(31444171))

        val article = output.result
        val parsedArticle = eFetchPubmedParser.parsePubmedArticlesIn(
            article.byteInputStream()
        ).getOrNull(0)
        val pmid = parsedArticle?.pmid?.toLong()
        val year = parsedArticle?.year
        val issue = parsedArticle?.issue
        val volume = parsedArticle?.volume

        assertEquals(31444171, pmid)
        assertEquals("2019", year)
        assertEquals("9", issue)
        assertEquals("63", volume)
    }
}
