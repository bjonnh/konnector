/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@KtorExperimentalAPI
@ExperimentalTime
internal class OtolTest {
    private var connector = OtolConnector(OfficialOtolAPI())

    @Test
    fun about() {
        val about = connector.taxonomy.about()
        assert(about.name == "ott")
    }

    @Test
    fun `basic taxon info`() {
        val taxInfo = connector.taxonomy.taxonInfo(515698)
        assert(taxInfo.unique_name == "Barnadesia")
    }

    @Test
    fun `taxon info with lineage`() {
        val taxInfo = connector.taxonomy.taxonInfo(515698, includeLineage = true)
        assert(taxInfo.lineage?.any { it.name == "Pentapetalae" } ?: false)
    }
}