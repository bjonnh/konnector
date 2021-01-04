/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@KtorExperimentalAPI
@ExperimentalTime
internal class GlobalNamesVerifyConnectorTest {
    val MINIMAL_NUMBER_OF_SOURCES = 100

    private var connector = GlobalNamesVerifyConnector(OfficialGlobalNamesVerifyAPI())

    @Test
    fun ping() {
        assert(connector.ping())
    }

    @Test
    fun version() {
        val version = connector.version()
        assert(version.version.isNotEmpty())
        assert(version.build.startsWith("20"))
    }

    @Test
    fun dataSources() {
        val sources = connector.dataSources()
        assert(sources.size > MINIMAL_NUMBER_OF_SOURCES)
    }

    @Test
    fun dataSource() {
        val source = connector.dataSource(1)
        assert(source.title == "Catalogue of Life")
    }

    @Test
    fun verifications() {
        val source = connector.verifications(
            VerificationQuery(
                nameStrings = listOf(
                    "Pomatomus soltator",
                    "Bubu bubo (Linnaeus, 1758)"  // The error here is on purpose, so we get no preferred result
                ), preferredSources = listOf(1, 12, 169), withVernaculars = false
            )
        )
        assert(source.size == 2)
        assert(source.filter { it.input == "Pomatomus soltator" }
            .first().bestResult?.currentName == "Pomatomus saltatrix (Linnaeus, 1766)")
    }
}