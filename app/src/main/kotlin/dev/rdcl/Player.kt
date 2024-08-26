package dev.rdcl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.rdcl.dto.Status
import dev.rdcl.dto.SyncStatus
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

    private val statusPollIntervalWhenPlaying: Int = 30,
    private val statusPollIntervalWhenNotPlaying: Int = 270,
    private val syncStatusPollInterval: Int = 240,
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

    suspend fun onStatus(handler: Status.() -> Unit) {
        var status = getStatus()
        while (true) {
            handler(status)
            status = getStatus(status)
        }
    }

    suspend fun getStatus(status: Status? = null): Status = withContext(Dispatchers.IO) {
        httpClient.get {
            url(path = "/Status") {
                if (status !== null) {
                    parameters.append("etag", status.etag)
                    parameters.append(
                        "timeout",
                        when {
                            // poll more frequently when playing, to keep the progress bar accurate
                            status.isPlaying() -> statusPollIntervalWhenPlaying
                            else -> statusPollIntervalWhenNotPlaying
                        }.toString()
                    )
                }
            }
        }.body()
    }

    suspend fun onSyncStatus(handler: SyncStatus.() -> Unit) {
        var syncStatus = getSyncStatus()
        while (true) {
            handler(syncStatus)
            syncStatus = getSyncStatus(syncStatus)
        }
    }

    suspend fun getSyncStatus(syncStatus: SyncStatus? = null): SyncStatus = withContext(Dispatchers.IO) {
        httpClient.get {
            url(path = "/SyncStatus") {
                if (syncStatus !== null) {
                    parameters.append("etag", syncStatus.etag)
                    parameters.append("timeout", syncStatusPollInterval.toString())
                }
            }
        }.body()
    }
}
