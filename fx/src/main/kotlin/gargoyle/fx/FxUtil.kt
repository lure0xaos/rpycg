package gargoyle.fx

import gargoyle.fx.log.FxLog
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Window
import java.net.URL
import java.security.PrivilegedActionException
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import kotlin.reflect.KClass

object FxUtil {

    private val CONTROL = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
    private val PATTERN_FORMAT = ("\\{([^}{'\\s]+)}").toRegex()

    fun findResource(baseClass: KClass<*>, locale: Locale, baseName: String, suffixes: String): URL? {
        val classLoader = baseClass.java.classLoader
        val postfixes: Array<String> = when {
            locale.language.isNotEmpty() && locale.country.isNotEmpty() ->
                arrayOf("_${locale.language}_${locale.country}", "_${locale.language}", "")
            locale.language.isNotEmpty() && locale.country.isEmpty() ->
                arrayOf("_${locale.language}", "")
            locale.language.isEmpty() && locale.country.isEmpty() ->
                arrayOf("")
            else ->
                arrayOf("")
        }
        suffixes.split(",").forEach { suffix ->
            postfixes.forEach { postfix ->
                val resourceName = "$baseName$postfix.$suffix"
                val absoluteResourceName = if (resourceName.startsWith("/")) resourceName else "/$resourceName"
                arrayOf(
                    getResource(baseClass, resourceName),
                    getResource(classLoader, resourceName),
                    getResource(baseClass, absoluteResourceName),
                    getResource(classLoader, absoluteResourceName)
                ).forEach { url ->
                    url?.let {
                        return it.also {
                            FxLog.debug("found (${baseClass.qualifiedName})$baseName.$suffix (from $resourceName)")
                        }
                    }
                }
            }
        }
        FxLog.warn("Can''t find resource for $baseName[$suffixes]")
        FxLog.warn(
            "Does ${baseClass.java.module} opens ${
                baseName.trimStart('/').substringBeforeLast('/').replace('/', '.')
            }?"
        )
        return null
    }

    fun findScene(node: Node?): Scene? =
        node?.scene

    fun findScene(node: Window?): Scene? =
        node?.scene

    fun findStage(node: Node?): Stage? =
        with(node?.scene?.window) {
            when (this) {
                is Stage -> this
                else -> null
            }
        }

    fun format(formats: List<String>, values: Map<String, Any>): List<String> =
        formats.map { format: String -> format(format, values) }

    fun format(format: String, values: Map<String, Any>): String =
        PATTERN_FORMAT.replace(format) { values[it.groupValues[1]]?.toString() ?: it.groupValues[0] }

    fun getOrCreateScene(parent: Parent?): Scene =
        parent?.scene ?: Scene(parent)

    fun loadResources(baseClass: KClass<*>, locale: Locale, baseName: String): ResourceBundle? =
        try {
            FxIntUtil.doPrivileged {
                ResourceBundle.getBundle(baseName.trimStart('/'), locale, baseClass.java.classLoader)
            }
        } catch (e: MissingResourceException) {
            FxLog.error("Can''t find resources for $baseName")
            null
        } catch (e: PrivilegedActionException) {
            FxLog.error("Can''t load resources for $baseName", e)
            null
        }

    fun message(baseClass: KClass<*>, locale: Locale, code: String, args: Map<String, Any>): String =
        message(baseClass, locale, {
            FxLog.error("$code not found in resources of $baseClass")
            format(code, args)
        }, code, args)

    fun message(
        baseClass: KClass<*>,
        locale: Locale,
        defaultMessage: () -> String,
        code: String,
        args: Map<String, Any> = mapOf()
    ): String =
        loadResources(baseClass, locale, resolveBaseName(baseClass))?.let {
            if (it.containsKey(code)) format(it.getString(code), args) else defaultMessage()
        } ?: defaultMessage()

    fun resolveBaseName(component: FxComponent<*, *>, baseName: String): String =
        resolveBaseName(component.name, baseName)

    fun resolveBaseName(component: FxComponent<*, *>): String = component.name

    fun resolveBaseName(aClass: KClass<*>, baseName: String = aClass.java.simpleName): String =
        when {
            baseName.isEmpty() -> "/${aClass.java.getPackage().name.replace('.', '/')}"
            baseName.startsWith('/') -> baseName
            else -> "/${aClass.java.getPackage().name.replace('.', '/')}/$baseName"
        }

    fun resolveBaseName(baseName: String, name: String): String =
        when {
            baseName.isEmpty() || name.isEmpty() -> ""
            name.startsWith('/') -> name
            else -> "${baseName.substringBeforeLast('/')}/$name"
        }

    private fun getResource(classLoader: ClassLoader, resourceName: String): URL? =
        FxIntUtil.doPrivileged { classLoader.getResource(resourceName) }

    private fun getResource(baseClass: KClass<*>, resourceName: String): URL? =
        FxIntUtil.doPrivileged { baseClass.java.getResource(resourceName) }

    operator fun ResourceBundle.get(key: String, default: String = key): String =
        if (containsKey(key)) getString(key) else default

}
