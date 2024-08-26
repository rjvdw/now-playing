package dev.rdcl

import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("app")
private val player = Player(
    host = System.getenv("PLAYER_HOST") ?: throw NullPointerException("Missing required env: PLAYER_HOST"),
    port = System.getenv("PLAYER_PORT")?.toInt() ?: 11000,
    scheme = System.getenv("PLAYER_SCHEME") ?: "http",
)

suspend fun main(): Unit = coroutineScope {
    logger.info("Using player ${player.address}")
    var previousStatusText = ""
    player.onStatus { status ->
        val statusText = "[${status.state}] ${status.getTitle()}"
        if (previousStatusText != statusText) {
            logger.info(statusText)
        }
        previousStatusText = statusText
    }
}
