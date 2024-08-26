package dev.rdcl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("app")
private val player = Player(
    host = System.getenv("PLAYER_HOST") ?: throw NullPointerException("Missing required env: PLAYER_HOST"),
    port = System.getenv("PLAYER_PORT")?.toInt() ?: 11000,
    scheme = System.getenv("PLAYER_SCHEME") ?: "http",
)

suspend fun main(): Unit = coroutineScope {
    logger.info("Using player ${player.address}")

    val syncStatusJob = launch {
        var previousSyncStatusText = ""
        player.onSyncStatus {
            val syncStatusText = "Player: $name ($modelName)"
            if (previousSyncStatusText != syncStatusText) {
                logger.info(syncStatusText)
            }
            previousSyncStatusText = syncStatusText
        }
    }

    val statusJob = launch {
        var previousStatusText = ""
        player.onStatus {
            val statusText = "[$state] ${getTitle()}"
            if (previousStatusText != statusText) {
                logger.info(statusText)
            }
            previousStatusText = statusText
        }
    }

    syncStatusJob.join()
    statusJob.join()
}
