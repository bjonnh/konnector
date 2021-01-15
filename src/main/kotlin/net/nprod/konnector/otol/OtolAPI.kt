/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
interface OtolAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String
}
