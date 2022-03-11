package gargoyle.rpycg.service

import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil.resolveBaseName
import java.util.Locale
import java.util.ResourceBundle
import kotlin.reflect.KClass

class LocaleConverter internal constructor(baseClass: KClass<*>, locale: Locale) {
    private val resources: ResourceBundle = ResourceBundle.getBundle(
        resolveBaseName(LocaleConverter::class).trimStart { it == '/' },
        locale,
        baseClass.java.classLoader
    )

    constructor(context: FxContext) : this(context.baseClass, FxContext.current.locale)

    val locales: Set<Locale>
        get() = (resources.getString(KEY_SUPPORTED_LOCALES).split(",")).map { toLocale(it) }.toSet()

    fun getSimilarLocale(locale: Locale): Locale =
        getSimilarLocale(locales, locale)

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

    fun toString(locale: Locale): String {
        val hasLanguage = locale.language.isNotEmpty()
        val hasScript = locale.script.isNotEmpty()
        val hasCountry = locale.country.isNotEmpty()
        val hasVariant = locale.variant.isNotEmpty()
        val result = StringBuilder(locale.language)
        if (hasCountry || hasLanguage && (hasVariant || hasScript)) result.append('_').append(locale.country)
        if (hasVariant && (hasLanguage || hasCountry)) result.append('_').append(locale.variant)
        if (hasScript && (hasLanguage || hasCountry)) result.append("_#").append(locale.script)
        return result.toString()
    }

    private fun getSimilarLocale(locales: Collection<Locale>, locale: Locale): Locale {
        return locales.firstOrNull { locale.language == it.language }
            ?: locales.firstOrNull() ?: Locale.getDefault()
    }

    companion object {
        private const val KEY_SUPPORTED_LOCALES = "supported_locales"
    }
}
