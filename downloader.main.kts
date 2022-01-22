#!/usr/bin/env kotlin

@file:DependsOn("commons-io:commons-io:2.11.0")
@file:DependsOn("commons-codec:commons-codec:1.15")
@file:DependsOn("org.jsoup:jsoup:1.14.3")

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

val username = args[0]
val password = args[1]

val items = listOf(
    SimpleDownloader(
        name = "ES Speedcams",
        url = "https://www.todo-poi.es/radar/GARMIN_RADARES/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip",
        filename = "garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip"
    ),
    SimpleDownloader(
        name = "Mercadona POIs",
        url = "https://www.todo-poi.es/comercio/GARMIN/Mercadona.zip",
        filename = "Mercadona.zip"
    ),
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

data class PoiFile(
    val file: File,
    val hash: String?
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

class LufopDownloader(
    private val username: String,
    private val password: String
) : Downloader {
    override val name: String = "Lufop EU CSV"

    override fun download(baseDir: File): File {
        val file = File(baseDir, "Lufop-Zones-de-danger-EU-CSV.zip")

        val loginPage = Jsoup.connect("https://forum.lufop.net/ucp.php?mode=login")
            .method(Connection.Method.GET)
            .execute()

        val loginForm = loginPage.parse().getElementById("login") ?: error("No login form found!")
        val redirect = loginForm.getValueByName("redirect")
        val creationTime = loginForm.getValueByName("creation_time")
        val sid = loginForm.getValueByName("sid")
        val formToken = loginForm.getValueByName("form_token")

        val loginResult = Jsoup.connect("http://www.mikeportnoy.com/forum/login.aspx")
            .data("username", username)
            .data("password", password)
            .data("redirect", redirect)
            .data("creation_time", creationTime)
            .data("form_token", formToken)
            .data("sid", sid)
            .cookies(loginPage.cookies())
            .method(Connection.Method.POST)
            .followRedirects(false)
            .execute()

        val zipFileResponse = Jsoup.connect("https://lufop.net/wp-content/plugins/downloads-manager/upload/Lufop-Zones-de-danger-EU-CSV.zip")
            .cookies(loginResult.cookies())
            .method(Connection.Method.GET)
            .ignoreContentType(true)
            .execute()

        if (zipFileResponse.contentType() != "application/zip") {
            error("Unexpected content type ${zipFileResponse.contentType()}")
        }
        zipFileResponse.bodyStream().use { bodyStream ->
            FileUtils.copyInputStreamToFile(bodyStream, file)
        }

        return file
    }

    private fun Element.getValueByName(name: String): String =
        getElementsByAttributeValue("name", name).firstOrNull()?.`val`() ?: error("No $name element found or null!")
}
