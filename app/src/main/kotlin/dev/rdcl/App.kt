package dev.rdcl

import dev.rdcl.nowplaying.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("app")
private val player = Player(
    host = System.getenv("PLAYER_HOST") ?: throw NullPointerException("Missing required env: PLAYER_HOST"),
    port = System.getenv("PLAYER_PORT")?.toInt() ?: 11000,
    scheme = System.getenv("PLAYER_SCHEME") ?: "http",
)

suspend fun main(): Unit = coroutineScope {
    logger.info("Using player ${player.address}")

    val jobs = listOf(
        launchSyncStatusJob(),
        launchStatusJob(),
        launchUserInputJob(),
    )

    // wait for any of the jobs to finish, and then cancel the rest of the jobs
    select { jobs.forEach { it.onJoin {} } }
    jobs.forEach { it.cancel() }
    jobs.joinAll()
}

fun CoroutineScope.launchSyncStatusJob() = launch {
    var previousSyncStatusText = ""
    player.onSyncStatus {
        val syncStatusText = "Player: $name ($modelName)"
        if (previousSyncStatusText != syncStatusText) {
            logger.info(syncStatusText)
        }
        previousSyncStatusText = syncStatusText
    }
}

fun CoroutineScope.launchStatusJob() = launch {
    var previousStatusText = ""
    player.onStatus {
        val statusText = "[$state] ${getTitle()}"
        if (previousStatusText != statusText) {
            logger.info(statusText)
        }
        previousStatusText = statusText
    }
}

fun CoroutineScope.launchUserInputJob() = launch {
    println("Starting interactive command prompt. Type help for more information.")
    while (true) {
        val input = readlnOrNull()?.trim()
        if (input.isNullOrBlank()) continue

        val parts = input.split(" ").map { it.trim() }.filter { it.isNotBlank() }
        val (command, arguments) = parts.firstOrNull() to parts.drop(1)

        when (command) {
            "exit" -> {
                // exit the program
                break
            }

            "help" -> {
                println(
                    """
                    Supported commands:
                      exit        Close the program.
                      help        Print this help message.
                      play        Start playback.
                      pause       Pause playback.
                      stop        Stop playback.
                      volume      Show the current volume.
                      volume <x>  Set volume to x.
                      playlist    Get current playlist.
                """.trimIndent()
                )
            }

            // playback
            "play" -> player.play()
            "pause" -> player.pause()
            "stop" -> player.stop()

            // volume
            "volume" -> {
                val result = when {
                    arguments.isEmpty() -> player.getVolume()
                    else -> player.setVolume(arguments.first().toInt())
                }
                println("Volume: ${result.level}")
            }

            // playlist
            "playlist" -> player.getPlaylist()
                .songs
                .forEach { println("${it.id + 1}. ${it.title}") }
        }
    }
}
