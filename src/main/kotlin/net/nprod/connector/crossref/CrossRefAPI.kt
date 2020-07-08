package net.nprod.connector.crossref

import io.ktor.util.KtorExperimentalAPI
import net.nprod.connector.commons.WebAPI

@KtorExperimentalAPI
interface CrossRefAPI: WebAPI {
    val apiURL: String
}
