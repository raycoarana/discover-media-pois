import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.net.URL

class SimpleDownloader(
    override val name: String,
    private val url: String,
    private val filename: String
) : Downloader {
    override fun download(baseDir: File): File {
        val destination = File(baseDir, filename)
        val zipFileResponse = Jsoup.connect(url)
            .method(Connection.Method.GET)
            .ignoreContentType(true)
            .execute()
        if (zipFileResponse.contentType() != "application/zip") {
            error("Unexpected content type ${zipFileResponse.contentType()}")
        }
        zipFileResponse.bodyStream().use { bodyStream ->
            FileUtils.copyInputStreamToFile(bodyStream, destination)
        }
        return destination
    }
}
