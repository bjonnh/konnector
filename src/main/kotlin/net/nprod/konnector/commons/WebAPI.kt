package net.nprod.konnector.commons

/*import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.IO.Companion.raiseError
import arrow.fx.extensions.fx*/
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger


@KtorExperimentalAPI
interface WebAPI {
    val log: Logger

    var delayTime: Long  // Delay in ms between each request
    var lastQueryTime: Long
    var httpClient: HttpClient

    /**
     * Calculates the necessary delay in milliseconds by using the last time a query was made and the
     * necessary delayTime
     */
    fun calcDelay(): Long {
        val lastQueryDelay = System.currentTimeMillis() - lastQueryTime
        return (delayTime - lastQueryDelay).coerceAtLeast(0)
    }

    /**
     * Updates the last query time
     */
    fun updateLastQueryTime() {
        lastQueryTime = System.currentTimeMillis()
    }

    fun delayUpdate(call: HttpResponse) {
        updateLastQueryTime()
    }

    fun call(url: String, parameters: MutableMap<String, String>? = null, retries: Int=3): String {
        log.debug("Connecting to $url")

        return try {
            val call = runBlocking {
                delay(calcDelay())
                val response = httpClient.request<HttpResponse>(url) {
                    method = HttpMethod.Get
                    parameters?.forEach { k, v -> parameter(k, v) }
                }
                delayUpdate(response)
                when (response.status.value) {
                    200 -> response.readText()
                    404 -> throw NonExistentReference
                    400 -> throw BadRequestError(response.readText())
                    429 -> {
                        delay(2_000)
                        throw TooManyRequests
                    } // We block for 2s in case of rate limiting trigger
                    else -> throw UnManagedReturnCode(response.status.value)
                }
            }
            call
        } catch (e: KnownError) {
            if (retries>0) return call(url, parameters, retries - 1)
            throw e
        }
    }

    fun newClient(module: String) = HttpClient(CIO) {
        expectSuccess = false
        engine {
            threadsCount = 4

            with(endpoint) {
                maxConnectionsPerRoute = 100

                pipelineMaxSize = 20

                keepAliveTime = 5000

                connectTimeout = 5000

                connectRetryAttempts = 5

            }
        }
    }
}