@file:Suppress("DEPRECATION")

package gargoyle.fx

import javafx.application.Application
import javafx.application.HostServices
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Hyperlink
import javafx.stage.Stage
import java.io.Closeable
import java.net.URL
import java.nio.charset.Charset
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.Locale
import java.util.ResourceBundle
import java.util.prefs.Preferences
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
class FxContext(
    val registry: FxRegistry,
    val baseClass: KClass<*>,
    val charset: Charset,
    val locale: Locale,
    val skin: String,
    val preferences: Preferences = Preferences.userNodeForPackage(baseClass.java)
) : Closeable {
    fun alert(message: String, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.alert(this, stage, message, *buttons)

    fun alert(owner: Stage, message: String, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.alert(this, owner, message, *buttons)

    fun alert(message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.alert(this, stage, message, buttons)

    fun alert(owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.alert(this, owner, message, buttons)

    fun asButtonTypeArray(buttons: Map<ButtonData, String>): Array<ButtonType> = FxDialogs.asButtonTypeArray(buttons)

    fun asButtonTypeCollection(buttons: Map<ButtonData, String>): List<ButtonType> =
        FxDialogs.asButtonTypeCollection(buttons)

    fun ask(message: String): Boolean? = FxDialogs.ask(this, stage, message)

    fun ask(owner: Stage, message: String): Boolean? = FxDialogs.ask(this, owner, message)

    override fun close() {}
    fun confirm(message: String): Boolean = FxDialogs.confirm(this, stage, message)

    fun confirm(owner: Stage, message: String): Boolean = FxDialogs.confirm(this, owner, message)

    fun confirm(message: String, buttons: Map<ButtonData, String>): Boolean =
        FxDialogs.confirm(this, stage, message, buttons)

    fun confirm(owner: Stage, message: String, buttons: Map<ButtonData, String>): Boolean =
        FxDialogs.confirm(this, owner, message, buttons)

    fun confirmExt(message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.confirmExt(this, stage, message, buttons)

    fun confirmExt(owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.confirmExt(this, owner, message, buttons)

    fun createHyperlink(text: String, action: (Hyperlink) -> Unit): Hyperlink =
        FxDialogs.createHyperlink(text, action)

    fun <R : Any?> decorateDialog(
        dialog: Dialog<R>,
        resultConverter: (ButtonType?) -> R?,
        buttons: Map<ButtonData, String>,
        title: String
    ) = FxDialogs.decorateDialog(this, dialog, resultConverter, buttons, title)

    fun <R : Any?> decorateDialog(
        dialog: Dialog<R>,
        resultConverter: (ButtonType?) -> R?,
        buttons: Map<ButtonData, String>,
        title: String,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) = FxDialogs.decorateDialog(this, dialog, resultConverter, buttons, title, detailsButtonDecorator)

    fun decorateDialogAs(dialog: Alert, buttons: Map<ButtonData, String>) =
        FxDialogs.decorateDialogAs(this, stage, dialog, buttons)

    fun decorateDialogAs(owner: Stage, dialog: Alert, buttons: Map<ButtonData, String>) =
        FxDialogs.decorateDialogAs(this, owner, dialog, buttons)

    fun dialog(
        type: AlertType,
        message: String,
        titleProvider: () -> String,
        buttons: Map<ButtonData, String>
    ): ButtonType? =
        FxDialogs.dialog(this, stage, type, message, titleProvider, buttons)

    fun dialog(
        owner: Stage,
        type: AlertType,
        message: String,
        titleProvider: () -> String,
        buttons: Map<ButtonData, String>
    ): ButtonType? =
        FxDialogs.dialog(this, owner, type, message, titleProvider, buttons)

    fun error(message: String, ex: Exception?, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(this, stage, message, ex, *buttons)

    fun error(owner: Stage, message: String, ex: Exception?, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(this, owner, message, ex, *buttons)

    fun error(message: String, ex: Exception?, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.error(this, stage, message, ex, buttons)

    fun error(owner: Stage, message: String, ex: Exception?, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.error(this, owner, message, ex, buttons)

    fun error(message: String, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(this, stage, message, *buttons)

    fun error(owner: Stage, message: String, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(this, owner, message, *buttons)

    fun error(message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.error(this, stage, message, buttons)

    fun error(owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? =
        FxDialogs.error(this, owner, message, buttons)

    fun findResource(component: FxComponent<*, *>, suffix: String): URL? =
        FxUtil.findResource(baseClass, locale, component.name, suffix)

    fun findResource(baseClass: KClass<*>, suffix: String): URL? =
        FxUtil.findResource(baseClass, locale, resolveBaseName(baseClass), suffix)

    fun findResource(baseName: String, suffix: String): URL? =
        FxUtil.findResource(baseClass, locale, baseName, suffix)

    fun findResource(baseClass: KClass<*>, baseName: String, suffix: String): URL? =
        FxUtil.findResource(baseClass, locale, FxUtil.resolveBaseName(baseClass, baseName), suffix)

    fun <T : Any> getBean(type: KClass<T>): T =
        registry.getBean(type)

    fun <T : Any> getBean(name: String, type: KClass<T>): T =
        registry.getBean(name, type)

    val hostServices: HostServices
        get() = getBean(HostServices::class.qualifiedName!!, HostServices::class)
    val parameters: Application.Parameters
        get() = getBean(Application.Parameters::class.qualifiedName!!, Application.Parameters::class)

    val stage: Stage
        get() = getBean(Stage::class.qualifiedName!!, Stage::class)

    fun initializeContext(hostServices: HostServices, parameters: Application.Parameters) {
        register(HostServices::class, hostServices)
        register(Application.Parameters::class, parameters)
    }

    fun <C : V, V : Parent> loadComponent(component: C): FxComponent<C, V>? =
        FxIntUtil.loadComponent(this, resolveBaseName(component::class), component, component)

    fun <C : V, V : Parent> loadComponent(componentClass: KClass<out C>): FxComponent<C, V>? {
        val component = registerAndGet(componentClass)
        return FxIntUtil.loadComponent(this, resolveBaseName(component::class), component, component)
    }

    fun <C : Any, V : Parent> loadComponent(baseName: String, controller: C, root: V): FxComponent<C, V>? =
        FxIntUtil.loadComponent(this, baseName, controller, root)

    fun <C : Any, V : Parent> loadComponent(baseName: String): FxComponent<C, V>? =
        FxIntUtil.loadComponent(this, baseName, null, null)

    fun <D : Dialog<*>, V : Parent> loadDialog(dialog: D): FxComponent<D, V>? =
        FxIntUtil.loadDialog(this, dialog)

    fun loadResources(aClass: KClass<*>): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, resolveBaseName(aClass))

    fun loadResources(component: FxComponent<*, *>): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, component.name)

    fun loadResources(baseName: String): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, baseName)

    fun prompt(message: String): String =
        FxDialogs.prompt(this, stage, message)

    fun prompt(owner: Stage, message: String): String =
        FxDialogs.prompt(this, owner, message)

    fun resolveBaseName(baseName: String, name: String): String =
        FxUtil.resolveBaseName(baseName, name)

    fun resolveBaseName(aClass: KClass<*>): String =
        FxUtil.resolveBaseName(aClass)

    fun resolveBaseName(aClass: KClass<*>, baseName: String): String =
        FxUtil.resolveBaseName(aClass, baseName)

    fun showDocument(uri: String) {
        hostServices.showDocument(uri)
    }

    fun toBuilder(): FxContextBuilder =
        builder0.copyOf()
            .setRegistry(registry)
            .setBaseClass(baseClass)
            .setCharset(charset)
            .setLocale(locale)
            .setSkin(skin)

    fun <D : Any, I : D> register(name: String, declarationClass: KClass<D>, implementationClass: KClass<I>) =
        registry.register(name, declarationClass, implementationClass)

    fun <D : Any, I : D> register(
        name: String,
        declarationClass: KClass<D>,
        implementationClass: KClass<I>,
        existingBean: I
    ) = registry.register(name, declarationClass, implementationClass, existingBean)

    fun <T : Any> register(type: KClass<T>, existingBean: T) =
        register(type.qualifiedName!!, type, type, existingBean)

    fun <T : Any> registerAndGet(type: KClass<T>): T {
        register(type.qualifiedName!!, type, type)
        return getBean(type)
    }


    override fun toString(): String =
        "FXContextImpl{baseClass=${baseClass}, charset=${charset}, locale=${locale}, skin=${skin}}"

    companion object {
        private const val FACTORY = "java.util.prefs.PreferencesFactory"

        internal lateinit var builder0: FxContextBuilder
        private lateinit var instance: FxContext

        var current: FxContext
            get() {
                if (!::instance.isInitialized) instance = builder().build()
                return instance
            }
            set(value) {
                instance = value
            }

        init {
            AccessController.doPrivileged(PrivilegedAction<Void> {
                System.setProperty(FACTORY, FxFilePreferencesFactory::class.qualifiedName!!)
                null
            })
        }

        fun builder(): FxContextBuilder = if (::builder0.isInitialized) builder0.copyOf() else FxContextBuilder()

        @Suppress("MemberVisibilityCanBePrivate")
        class FxContextBuilder internal constructor() {
            lateinit var baseClass: KClass<*>
                private set
            var charset: Charset = Charsets.UTF_8
                private set
            lateinit var locale: Locale
                private set
            var registry = FxRegistry()
                private set
            var skin: String = ""
                private set

            fun build(): FxContext =
                FxContext(registry, baseClass, charset, locale, skin)

            fun setBaseClass(baseClass: KClass<*>): FxContextBuilder {
                this.baseClass = baseClass
                return this
            }

            fun setCharset(charset: Charset): FxContextBuilder {
                this.charset = charset
                return this
            }

            fun setLocale(locale: Locale): FxContextBuilder {
                this.locale = locale
                return this
            }

            fun setSkin(skin: String): FxContextBuilder {
                this.skin = skin
                return this
            }

            fun setRegistry(registry: FxRegistry): FxContextBuilder {
                this.registry = registry
                return this
            }

            fun copyOf(): FxContextBuilder =
                FxContextBuilder()
                    .setRegistry(registry)
                    .setBaseClass(baseClass)
                    .setCharset(charset)
                    .setLocale(locale)

        }
    }

}
