package gargoyle.rpycg.service

import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil.get
import java.util.Locale
import java.util.ResourceBundle

class LocaleConverter internal constructor(context: FxContext) {

    private val resources: ResourceBundle =
        context.loadResources(LocaleConverter::class) ?: error("no resources {LocaleConverter}")

    val locales: Set<Locale>
        get() = resources[KEY_SUPPORTED_LOCALES].split(",").map { toLocale(it) }.toSet()

    fun getSimilarLocale(locale: Locale): Locale =
        locales.firstOrNull { locale.language == it.language } ?: locales.firstOrNull() ?: Locale.getDefault()

    fun toDisplayString(locale: Locale): String =
        "${locale.getDisplayLanguage(FxContext.current.locale)} (${locale.getDisplayLanguage(locale)})"

    fun toLocale(loc: String): Locale {
        val parts = loc.trim().split("_")
        return when (parts.size) {
            3 -> Locale(parts[0], parts[1], parts[2])
            2 -> Locale(parts[0], parts[1])
            1 -> Locale(parts[0])
            0 -> Locale(loc)
            else -> Locale(loc)
        }
    }

    fun toString(locale: Locale): String =
        with(mutableListOf<String>(locale.language)) {
            if (locale.country.isNotEmpty() || locale.language.isNotEmpty() && (locale.variant.isNotEmpty() || locale.script.isNotEmpty()))
                this += locale.country
            if (locale.variant.isNotEmpty() && (locale.language.isNotEmpty() || locale.country.isNotEmpty()))
                this += locale.variant
            if (locale.script.isNotEmpty() && (locale.language.isNotEmpty() || locale.country.isNotEmpty()))
                this += "#${locale.script}"
            this
        }.joinToString("_")

    companion object {
        private const val KEY_SUPPORTED_LOCALES = "supported_locales"
    }
}
