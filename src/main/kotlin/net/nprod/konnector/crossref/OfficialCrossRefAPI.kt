package net.nprod.konnector.crossref


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.LoggerFactory


class OfficialCrossRefAPI : CrossRefAPI {
    override val log = LoggerFactory.getLogger(this::class.java)
    override var httpClient = newClient("net.nprod.connector.crossref")
    override var delayTime = 20L  // Delay in ms between each request
    override var lastQueryTime: Long = System.currentTimeMillis()
    override var apiURL = "https://api.crossref.org"

    /**
     * Updates the necessary delay from the HTTP headers received
     *
     * @param limit Is the number of elements
     * @param interval Is the period in the format <number>s (currently we have only seen 1s)
     *
     */
    private fun updateDelayFromHeaderData(limit: String? = "50", interval: String? = "1s") {
        val intervalInt = interval?.filter { it != 's' }?.toIntOrNull()
        val limitInt = limit?.toLongOrNull()
        if ((intervalInt != null) && (limitInt != null))
            delayTime = 1_000 * intervalInt / limitInt
    }

    /**
     * We use a different approach in that module as the API can tell us to slow down (and they do)
     */
    override fun delayUpdate(call: HttpResponse) {

        updateDelayFromHeaderData(
            call.headers["X-Rate-Limit-Limit"],
            call.headers["X-Rate-Limit-Interval"]
        )
        updateLastQueryTime()
    }

    @KtorExperimentalAPI
    override fun newClient(module: String) = HttpClient(CIO) {
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

