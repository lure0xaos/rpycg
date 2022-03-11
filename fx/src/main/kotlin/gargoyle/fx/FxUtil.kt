@file:Suppress("DEPRECATION")

package gargoyle.fx

import gargoyle.fx.log.FxLog
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Window
import java.net.URL
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import kotlin.reflect.KClass

@Suppress("unused")
object FxUtil {

    private val CONTROL = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
    private val PATTERN_FORMAT = ("\\{([^}{'\\s]+)}").toRegex()
    fun findResource(baseClass: KClass<*>, locale: Locale, baseName: String, suffixes: String): URL? {
        val classLoader = baseClass.java.classLoader
        val candidateLocales = CONTROL.getCandidateLocales(baseName, locale)
        for (suffix in suffixes.split(",").toTypedArray()) {
            for (specificLocale in candidateLocales) {
                val bundleName = CONTROL.toBundleName(baseName, specificLocale)
                val resourceName = CONTROL.toResourceName(bundleName, suffix)
                val absoluteResourceName =
                    if (resourceName.isNotEmpty() && '/' == resourceName[0]) resourceName else "/$resourceName"
                for (url in arrayOf(
                    getResource(baseClass, resourceName),
                    getResource(classLoader, resourceName),
                    getResource(baseClass, absoluteResourceName),
                    getResource(classLoader, absoluteResourceName)
                )) {
                    if (null != url) {
                        FxLog.debug("found (${baseClass.qualifiedName})${baseName}.${suffix} (from ${resourceName})")
                        return url
                    }
                }
            }
        }
        FxLog.warn("Can''t find resource for ${baseName}[${suffixes}]")
        FxLog.warn("Does ${baseClass.java.module} opens ${getPackage(baseName)}?")
        return null
    }

    private fun getPackage(baseName: String): String {
        val relative = if (baseName.isNotEmpty() && baseName[0] == '/') baseName.substring(1) else baseName
        val endIndex = relative.lastIndexOf('/')
        val substring = if (endIndex < 0) relative else relative.substring(0, endIndex)
        return substring.replace('/', '.')
    }

    fun findScene(node: Node?): Scene? = node?.scene

    fun findScene(node: Window?): Scene? = node?.scene

    fun findStage(node: Node?): Stage? = if (node?.scene?.window is Stage) (node.scene?.window) as Stage? else null

    fun format(formats: List<String>, values: Map<String, *>): List<String> =
        formats.map { format: String -> format(format, values) }

    fun format(format: String, values: Map<String, *>): String {
        return PATTERN_FORMAT.replace(format) { match: MatchResult ->
            val value = values[match.groupValues[1]]
            value?.toString() ?: match.groupValues[0]
        }
    }

    fun getOrCreateScene(parent: Parent?): Scene =
        parent?.scene ?: Scene(parent)

    fun loadResources(baseClass: KClass<*>, locale: Locale, baseName: String): ResourceBundle? =
        try {
            (AccessController.doPrivileged(PrivilegedExceptionAction {
                ResourceBundle.getBundle(
                    if (baseName[0] == '/') baseName.substring(1) else baseName, locale, baseClass.java.classLoader
                )
            } as PrivilegedExceptionAction<ResourceBundle>))
        } catch (e: MissingResourceException) {
            FxLog.error("Can''t find resources for $baseName")
            null
        } catch (e: PrivilegedActionException) {
            FxLog.error(e, "Can''t load resources for $baseName")
            null
        }

    fun message(
        baseClass: KClass<*>, locale: Locale, code: String, args: Map<String, *>
    ): String {
        return message(baseClass, locale, {
            FxLog.error("$code not found in resources of $baseClass")
            format(code, args)
        }, code, args)
    }

    fun message(
        baseClass: KClass<*>, locale: Locale, defaultMessage: () -> String, code: String, args: Map<String, *>
    ): String {
        return loadResources(baseClass, locale, resolveBaseName(baseClass))?.let {
            if (it.containsKey(code)) format(it.getString(code), args)
            else defaultMessage()
        } ?: defaultMessage()
    }

    fun resolveBaseName(component: FxComponent<*, *>, baseName: String): String =
        resolveBaseName(component.name, baseName)

    fun resolveBaseName(component: FxComponent<*, *>): String = component.name

    fun resolveBaseName(aClass: KClass<*>, baseName: String = aClass.java.simpleName): String =
        if (baseName.isEmpty()) {
            "/${aClass.java.getPackage().name.replace('.', '/')}"
        } else if ('/' == baseName[0]) {
            baseName
        } else {
            "/${aClass.java.getPackage().name.replace('.', '/')}/$baseName"
        }

    fun resolveBaseName(baseName: String, name: String): String = when {
        baseName.isEmpty() || name.isEmpty() -> ""
        '/' == name[0] -> name
        else -> "${baseName.substring(0, baseName.lastIndexOf('/'))}/$name"
    }

    private fun getResource(classLoader: ClassLoader, resourceName: String): URL? =
        AccessController.doPrivileged(PrivilegedAction { classLoader.getResource(resourceName) })

    private fun getResource(baseClass: KClass<*>, resourceName: String): URL? =
        AccessController.doPrivileged(PrivilegedAction { baseClass.java.getResource(resourceName) })


}
