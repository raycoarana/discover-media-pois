import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File

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

        val loginResult = Jsoup.connect("https://lufop.net/forum/ucp.php?mode=login&sid=$sid")
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
