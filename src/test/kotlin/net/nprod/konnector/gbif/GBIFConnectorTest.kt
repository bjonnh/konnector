package net.nprod.konnector.gbif

import io.ktor.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class GBIFConnectorTest {
    private var connector = GBIFConnector(OfficialGBIFAPI())

    @Test
    fun `taxon occurrence search`() {
        val output = connector.occurenceOfTaxon("1",
        offset=42)
        println(output)
        assertEquals(42, output.offset)
    }
}