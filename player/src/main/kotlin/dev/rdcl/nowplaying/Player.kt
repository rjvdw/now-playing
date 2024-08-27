package dev.rdcl.nowplaying

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.rdcl.nowplaying.dto.*
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

private val XML_MAPPER = XmlMapper()
    .registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

class Player(
    /**
     * The host on which the player can be reached.
     */
    host: String,

    /**
     * The port the player is listening on. Usually 11000.
     */
    port: Int,

    /**
     * The scheme used by the player. Usually `http`.
     */
    scheme: String,

    /**
     * The request timeout to use in the HTTP client. This value is in milliseconds and should be larger than any
     * timeout used when long-polling.
     */
    requestTimeout: Long = 500000,

    /**
     * The timeout (in seconds) to use when polling the `Status` while the player is playing. Since some fields can be
     * updated without changing the `etag`, it is probably wise to not set this timeout too high. This value may not be
     * set to 300 or higher.
     */
    private val statusPollIntervalWhenPlaying: Int = 30,

    /**
     * The timeout (in seconds) to use when polling the `Status` while the player is not playing. Since the fields that
     * can be updated without changing the `etag` are not expected to change while the player is not playing, it is
     * probably wise to set this timeout to a higher value. This value may not be set to 300 or higher.
     */
    private val statusPollIntervalWhenNotPlaying: Int = 270,

    /**
     * The timeout (in seconds) to use when polling the `SyncStatus`. This value may not be set to 300 or higher.
     */
    private val syncStatusPollInterval: Int = 240,
) {

    val address = "$scheme://$host:$port"

    private val httpClient: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            // increase request timeout to allow for long polling
            requestTimeoutMillis = requestTimeout
        }
        install(ContentNegotiation) {
            register(ContentType.Text.Xml, JacksonConverter(XML_MAPPER, true))
        }
        defaultRequest {
            url(
                scheme = scheme,
                host = host,
                port = port,
            )
        }
    }

    /**
     * Query volume and playback information. When a previous `Status` is provided, its `etag` is added to the request,
     * making this method wait until either a change occurs in the player, or the configured timeout expires.
     */
    suspend fun getStatus(status: Status? = null): Status = call("/Status") {
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

    /**
     * Query player information and player grouping information. When a previous `SyncStatus` is provided, its `etag` is
     * added to the request, making this method wait until either a change occurs in the player, or the configured
     * timeout expires.
     */
    suspend fun getSyncStatus(syncStatus: SyncStatus? = null): SyncStatus = call("/SyncStatus") {
        if (syncStatus !== null) {
            parameters.append("etag", syncStatus.etag)
            parameters.append("timeout", syncStatusPollInterval.toString())
        }
    }

    /**
     * Start playback of the current audio source.
     */
    suspend fun play(): State = call("/Play")

    /**
     * Pause the current playing audio.
     */
    suspend fun pause(): State = call("/Pause")

    /**
     * Stop the current playing audio.
     */
    suspend fun stop(): State = call("/Stop")

    /**
     * Retrieve the current volume settings.
     */
    suspend fun getVolume(): Volume = call("/Volume")

    /**
     * Set the volume level to a new value.
     */
    suspend fun setVolume(level: Int): Volume = call("/Volume") {
        parameters.append("level", level.toString())
    }

    /**
     * Retrieve the current playlist and information on all tracks in the playlist.
     */
    suspend fun getPlaylist(): Playlist = call("/Playlist")

    /**
     * Start long-polling the `Status`, and call the provided callback every time an update is received. This can either
     * be because the etag has changed (i.e. some fields in the `Status` have changed), or because the configured
     * timeout has expired.
     */
    suspend fun onStatus(handler: Status.() -> Unit) {
        var status = getStatus()
        while (true) {
            handler(status)
            status = getStatus(status)
        }
    }

    /**
     * Start long-polling the `SyncStatus`, and call the provided callback every time an update is received. This can
     * either be because the etag has changed (i.e. some fields in the `SyncStatus` have changed), or because the
     * configured timeout has expired.
     */
    suspend fun onSyncStatus(handler: SyncStatus.() -> Unit) {
        var syncStatus = getSyncStatus()
        while (true) {
            handler(syncStatus)
            syncStatus = getSyncStatus(syncStatus)
        }
    }

    private suspend inline fun <reified T> call(path: String, noinline block: URLBuilder.() -> Unit = {}): T =
        withContext(Dispatchers.IO) {
            httpClient.get {
                url(path = path, block = block)
            }.body()
        }
}
