import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val username = args[0]
    val password = args[1]

    val items = listOf(
        LufopDownloader(username = username, password = password)
    )

    val baseDir = File("sources")

    val poiFiles = items.map { downloader ->
        println("Starting download of ${downloader.name}")

        val downloadedFile = downloader.download(baseDir)

        println("Downloaded file at ${downloadedFile.absolutePath}")

        val newHash = downloadedFile.updateHashIfNew()
        PoiFile(downloadedFile, newHash)
    }

    if (poiFiles.any { it.hash != null  }) {
        println("New POIs downloaded.")
        exitProcess(0)
    } else {
        println("No new POIs or SpeedCams, aborting...")
        exitProcess(-1)
    }
}
