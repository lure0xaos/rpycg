package gargoyle.rpycg.service

import gargoyle.fx.FxUtil.format
import java.util.ResourceBundle

object ErrorMailer {
    private const val KEY_BODY = "mail.body"
    private const val KEY_EMAIL = "mail.email"
    private const val KEY_SUBJECT = "mail.subject"
    private val resourceBundleHolder: ResourceBundle =
        ResourceBundle.getBundle(ErrorMailer::class.qualifiedName!!.replace('.', '/'))

    fun mailError(e: Exception) =
        SendMail.mail(
            resourceBundleHolder.getString(KEY_EMAIL),
            resourceBundleHolder.getString(KEY_SUBJECT),
            format(resourceBundleHolder.getString(KEY_BODY), mapOf("stacktrace" to e.stackTraceToString()))
        )
}
