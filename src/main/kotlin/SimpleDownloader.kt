import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

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
