/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
interface GlobalNamesVerifyAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String
}
