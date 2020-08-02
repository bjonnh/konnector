package net.nprod.konnector.crossref

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI

@KtorExperimentalAPI
interface CrossRefAPI: WebAPI {
    val apiURL: String
}
