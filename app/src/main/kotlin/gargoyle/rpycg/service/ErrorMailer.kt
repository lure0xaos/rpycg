package gargoyle.rpycg.service

import gargoyle.fx.FxUtil
import gargoyle.fx.FxUtil.get
import java.util.ResourceBundle

object ErrorMailer {
    private const val KEY_BODY = "mail.body"
    private const val KEY_EMAIL = "mail.email"
    private const val KEY_SUBJECT = "mail.subject"
    private val resourceBundleHolder: ResourceBundle =
        ResourceBundle.getBundle(ErrorMailer::class.qualifiedName!!.replace('.', '/'))

    fun mailError(e: Exception): Unit =
        SendMail.mail(
            resourceBundleHolder[KEY_EMAIL],
            resourceBundleHolder[KEY_SUBJECT],
            FxUtil.format(resourceBundleHolder[KEY_BODY], mapOf("stacktrace" to e.stackTraceToString()))
        )
}
