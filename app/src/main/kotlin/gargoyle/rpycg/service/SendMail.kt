package gargoyle.rpycg.service

import gargoyle.fx.log.FxLog
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URLEncoder

object SendMail {

    fun mail(email: String, subject: String, body: String) = try {
        Desktop.getDesktop()
            .mail(URI.create("mailto:${(email.encode())}?subject=${(subject.encode())}&body=${(body.encode())}"))
    } catch (e: UnsupportedOperationException) {
        FxLog.error(e, "mail")
    } catch (e: IOException) {
        FxLog.error(e, "mail")
    }

    private fun String.encode(): String =
        URLEncoder.encode(this, Charsets.UTF_8).replace("+", "%20")
}
