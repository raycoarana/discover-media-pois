import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File

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
