package dev.rdcl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Player(
    host: String,
    port: Int,
    scheme: String,
    requestTimeout: Long = 500000,

    private val pollIntervalWhenPlaying: Int = 30,
    private val pollIntervalWhenNotPlaying: Int = 270,
) {

    val address = "$scheme://$host:$port"

    companion object {
        private val xmlMapper = XmlMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val httpClient: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            // increase request timeout to allow for long polling
            requestTimeoutMillis = requestTimeout
        }
        install(ContentNegotiation) {
            register(ContentType.Text.Xml, JacksonConverter(xmlMapper, true))
        }
        defaultRequest {
            url(
                scheme = scheme,
                host = host,
                port = port,
            )
        }
    }

    suspend fun onStatus(handler: (status: Status) -> Unit) {
        var status = getStatus(null)
        while (true) {
            handler(status)
            status = getStatus(status)
        }
    }

    private suspend fun getStatus(status: Status?): Status = withContext(Dispatchers.IO) {
        httpClient.get {
            url(path = "/Status") {
                if (status !== null) {
                    parameters.append("etag", status.etag)
                    parameters.append(
                        "timeout",
                        when {
                            // poll more frequently when playing, to keep the progress bar accurate
                            status.isPlaying() -> pollIntervalWhenPlaying
                            else -> pollIntervalWhenNotPlaying
                        }.toString()
                    )
                }
            }
        }.body()
    }
}
