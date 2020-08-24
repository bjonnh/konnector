package net.nprod.konnector.gbif


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import org.slf4j.LoggerFactory


class OfficialGBIFAPI : GBIFAPI {
    override val log = KotlinLogging.logger(this::class.java.name)
    override var httpClient = newClient("net.nprod.konnector.gbif")
    override var delayTime = 20L  // Delay in ms between each request
    override var lastQueryTime: Long = System.currentTimeMillis()
    override var apiURL = "https://api.gbif.org/v1/"

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