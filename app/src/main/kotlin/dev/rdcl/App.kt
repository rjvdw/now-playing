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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private val PLAYER_SCHEME = System.getenv("PLAYER_SCHEME")
    ?: "http"
private val PLAYER_HOST = System.getenv("PLAYER_HOST")
    ?: throw NullPointerException("Missing required env: PLAYER_HOST")
private val PLAYER_PORT = System.getenv("PLAYER_PORT")?.toInt()
    ?: 11000

private val xmlMapper = XmlMapper()
    .registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

private val httpClient = HttpClient(CIO) {
    install(HttpTimeout) {
        // increase request timeout to allow for long polling
        requestTimeoutMillis = 500000
    }
    install(ContentNegotiation) {
        register(ContentType.Text.Xml, JacksonConverter(xmlMapper, true))
    }
    defaultRequest {
        url(
            scheme = PLAYER_SCHEME,
            host = PLAYER_HOST,
            port = PLAYER_PORT,
        )
    }
}

suspend fun main(): Unit = coroutineScope {
    println("Using player $PLAYER_SCHEME://$PLAYER_HOST:$PLAYER_PORT")
    var status = getStatus(null)
    var previousStatusText = ""

    while (true) {
        val title = listOfNotNull(status.title1, status.title2, status.title3)
            .joinToString(" - ")
        val statusText = "[${status.state}] ${title}"
        if (previousStatusText != statusText) {
            println(statusText)
        }
        previousStatusText = statusText

        status = getStatus(status)
    }
}

private suspend fun getStatus(previous: Status?): Status = withContext(Dispatchers.IO) {
    httpClient.get {
        url(path = "/Status") {
            if (previous !== null) {
                parameters.append("etag", previous.etag)
                parameters.append(
                    "timeout",
                    when (previous.state) {
                        "play", "stream" -> 30
                        else -> 270
                    }.toString()
                )
            }
        }
    }.body()
}
