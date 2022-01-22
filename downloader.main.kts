#!/usr/bin/env kotlin

@file:DependsOn("commons-io:commons-io:2.11.0")
@file:DependsOn("commons-codec:commons-codec:1.15")
@file:DependsOn("org.apache.commons:commons-compress:1.21")
@file:DependsOn("org.eclipse.jgit:org.eclipse.jgit:6.0.0.202111291000-r")

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

val releaseVersion = args[0]
println("Release: $releaseVersion")
exitProcess(-1)

val items = mapOf(
    PoiType.ES_ONLY to SimpleDownloader(
        name = "ES Speedcams",
        url = "https://www.todo-poi.es/radar/GARMIN_RADARES/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip",
        filename = "garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip"
    ),
    PoiType.EU to SimpleDownloader(
        name = "Mercadona POIs",
        url = "https://www.todo-poi.es/comercio/GARMIN/Mercadona.zip",
        filename = "Mercadona.zip"
    ),
    PoiType.EU to LufopDownloader()
)

val baseDir = File("sources")

val poiFiles = items.map { (type, downloader) ->
    println("Starting download of ${downloader.name}")

    val downloadedFile = downloader.download(baseDir)

    println("Downloaded file at ${downloadedFile.absolutePath}")

    val newHash = downloadedFile.updateHashIfNew()
    PoiFile(downloadedFile, newHash, type)
}

if (poiFiles.any { it.hash != null  }) {
    println("Preparing for release v$releaseVersion")

    TODO("Run generation")

    // Generate new files calling poi-to-discover-media
    // Generate release file
    // Commit/Push new hash files (should do in another script?)
} else {
    println("No new POIs or SpeedCams, aborting...")
    exitProcess(-1)
}

fun File.isUpdated(lastVersionHash: String?): Boolean {
    val newVersion = md5()
    return lastVersionHash != newVersion
}

fun File.updateHash(newHash: String) {
    FileUtils.write(File("sources", name), newHash, Charsets.UTF_8)
}

fun File.md5(): String =
    inputStream().use {
        DigestUtils.md5Hex(it)
    }

fun File.getLastVersionHash(): String? =
    takeIf { it.exists() }?.let { FileUtils.readFileToString(it, Charsets.UTF_8) }

fun File.updateHashIfNew(): String? {
    val lastVersionHashFile = File("sources", "${name}.md5")
    return md5()
        .takeIf { it != lastVersionHashFile.getLastVersionHash() }
        ?.also { lastVersionHashFile.updateHash(it) }
}

enum class PoiType {
    EU,
    ES_ONLY,
}

data class PoiFile(
    val file: File,
    val hash: String?,
    val type: PoiType
)

interface Downloader {
    val name: String
    fun download(baseDir: File): File
}

class SimpleDownloader(
    override val name: String,
    private val url: String,
    private val filename: String
) : Downloader {
    override fun download(baseDir: File): File {
        val destination = File(baseDir, filename)
        FileUtils.copyURLToFile(URL(url), destination, 30000, 30000)
        return destination
    }
}

class LufopDownloader : Downloader {
    override val name: String = "Lufop EU CSV"

    val lufopCsvUrl = "https://lufop.net/wp-content/plugins/downloads-manager/upload/Lufop-Zones-de-danger-EU-CSV.zip"
    val lufopCsvFilename = "Lufop-Zones-de-danger-EU-CSV.zip"

// Login => https://forum.lufop.net/ucp.php?mode=login
// Extract Cookies AND Login Form
/*
username: {{ secrets.LUFOP_USERNAME}}
password: {{ secrets.LUFOP_PASSWORD}}
redirect: ./ucp.php?mode=login
creation_time: 1642629667
form_token: 61d91110412184d28df01779857868f3fc4b8b03
sid: 8cccfbaa138547b113df0292475160c5
redirect: index.php
login: Acceso
*/

// POST with cookies AND form to https://forum.lufop.net/ucp.php?mode=login
// Extract cookies

// GET https://lufop.net/wp-content/plugins/downloads-manager/upload/Lufop-Zones-de-danger-EU-CSV.zip

    override fun download(baseDir: File): File {
        TODO("Not yet implemented")
    }
}
