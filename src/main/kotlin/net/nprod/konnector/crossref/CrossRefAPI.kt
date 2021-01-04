/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.crossref

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

/**
 * Interface for CrossRefAPIs, allows to mock
 */
@ExperimentalTime
@KtorExperimentalAPI
interface CrossRefAPI : WebAPI {
    /**
     * The endpoint URL
     */
    val apiURL: String
}
