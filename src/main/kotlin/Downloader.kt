import java.io.File

interface Downloader {
    val name: String
    fun download(baseDir: File): File
}
