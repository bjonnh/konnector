package net.nprod.connector.gnfinder

import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.Executors

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GNFinderClientTest {
    val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
    val client = GNFinderClient("localhost:15601", dispatcher)

    @Test
    fun ping() {
        assert(client.ping() == "pong")
    }

    @Test
    fun ver() {
        assert(client.ver().startsWith("v"))
    }

    @Test
    fun findNames() {
        assert("Curcuma longa" in client.findNames("The source of the compound, Curcuma longa, is a plant."))
    }

    @Test
    fun findNamesWithSources() {
        assert("Plantae|Tracheophyta|Liliopsida|Zingiberales|Zingiberaceae|Curcuma|Curcuma longa" in
            client.findNames(
                "The source of the compound, Curcuma longa, is a plant.",
                sources = (1..182),
                verification = true
            )
        )
    }

    @AfterAll
    internal fun done() {
        client.close()
        dispatcher.close()
    }
}