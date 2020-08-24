package net.nprod.konnector.gbif

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI

@KtorExperimentalAPI
interface GBIFAPI: WebAPI {
    val apiURL: String
}
