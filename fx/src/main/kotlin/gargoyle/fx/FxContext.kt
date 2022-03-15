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
import java.util.Locale
import java.util.ResourceBundle
import java.util.prefs.Preferences
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
class FxContext(
    val registry: FxRegistry,
    val baseClass: KClass<*>,
    val charset: Charset,
    val locale: Locale,
    val skin: String?,
    val preferences: Preferences = Preferences.userNodeForPackage(baseClass.java)
) : Closeable {
    fun alert(message: String, owner: Stage = stage, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.alert(message, this, owner, arrayOf(*buttons))

    fun alert(message: String, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.alert(message, this, stage, buttons)

    fun alert(message: String, owner: Stage = stage, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.alert(message, this, owner, buttons)

    fun ask(message: String, owner: Stage = stage): Boolean? =
        FxDialogs.ask(message, this, owner)

    fun confirm(message: String, owner: Stage = stage): Boolean =
        FxDialogs.confirm(message, this, owner)

    fun confirm(message: String, buttons: Map<ButtonData, String> = mapOf()): Boolean =
        FxDialogs.confirm(message, this, stage, buttons)

    fun confirm(message: String, owner: Stage = stage, buttons: Map<ButtonData, String> = mapOf()): Boolean =
        FxDialogs.confirm(message, this, owner, buttons)

    fun confirmExt(message: String, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.confirmExt(message, this, stage, buttons)

    fun confirmExt(message: String, owner: Stage = stage, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.confirmExt(message, this, owner, buttons)

    fun createHyperlink(text: String, action: (Hyperlink) -> Unit): Hyperlink =
        FxDialogs.createHyperlink(text, action)

    fun <R : Any?> decorateDialog(
        dialog: Dialog<R>,
        resultConverter: (ButtonType?) -> R?,
        buttons: Map<ButtonData, String>,
        title: String
    ): Unit = FxDialogs.decorateDialog(this, dialog, resultConverter, buttons, title)

    fun <R : Any?> decorateDialog(
        dialog: Dialog<R>,
        resultConverter: (ButtonType?) -> R?,
        buttons: Map<ButtonData, String>,
        title: String,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ): Unit = FxDialogs.decorateDialog(dialog, resultConverter, title, this, buttons, detailsButtonDecorator)

    fun decorateDialogAs(dialog: Alert, buttons: Map<ButtonData, String> = mapOf()): Unit =
        FxDialogs.decorateDialogAs(dialog, this, stage, buttons)

    fun decorateDialogAs(dialog: Alert, owner: Stage = stage, buttons: Map<ButtonData, String> = mapOf()): Unit =
        FxDialogs.decorateDialogAs(dialog, this, owner, buttons)

    fun dialog(
        message: String,
        type: AlertType = AlertType.INFORMATION,
        titleProvider: () -> String = { "" },
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        FxDialogs.dialog(message, type, this, stage, titleProvider, buttons)

    fun dialog(
        message: String,
        type: AlertType = AlertType.INFORMATION,
        titleProvider: () -> String = { "" },
        owner: Stage = stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        FxDialogs.dialog(message, type, this, owner, titleProvider, buttons)

    fun error(message: String, ex: Exception? = null, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(message, ex, this, stage, arrayOf(*buttons))

    fun error(message: String, ex: Exception? = null, owner: Stage = stage, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(message, ex, this, owner, arrayOf(*buttons))

    fun error(message: String, ex: Exception? = null, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.error(message, ex, this, stage, buttons)

    fun error(
        owner: Stage,
        message: String,
        ex: Exception? = null,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        FxDialogs.error(message, ex, this, owner, buttons)

    fun error(message: String, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(message, this, stage, arrayOf(*buttons))

    fun error(message: String, owner: Stage = stage, vararg buttons: ButtonType): ButtonType? =
        FxDialogs.error(message, this, owner, arrayOf(*buttons))

    fun error(message: String, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.error(message, this, stage, buttons)

    fun error(message: String, owner: Stage = stage, buttons: Map<ButtonData, String> = mapOf()): ButtonType? =
        FxDialogs.error(message, this, owner, buttons)

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

    fun <C : V, V : Parent> loadComponent(component: C): FxComponent<C, V>? =
        FxIntUtil.loadComponent(resolveBaseName(component::class), this, component, component)

    fun <C : V, V : Parent> loadComponent(componentClass: KClass<out C>): FxComponent<C, V>? {
        val component = registerAndGet(componentClass)
        return FxIntUtil.loadComponent(resolveBaseName(component::class), this, component, component)
    }

    fun <C : Any, V : Parent> loadComponent(baseName: String, controller: C, root: V): FxComponent<C, V>? =
        FxIntUtil.loadComponent(baseName, this, controller, root)

    fun <C : Any, V : Parent> loadComponent(baseName: String): FxComponent<C, V>? =
        FxIntUtil.loadComponent(baseName, this, null, null)

    fun <D : Dialog<*>, V : Parent> loadDialog(dialog: D): FxComponent<D, V>? =
        FxIntUtil.loadDialog(dialog, this)

    fun loadResources(aClass: KClass<*>): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, resolveBaseName(aClass))

    fun loadResources(component: FxComponent<*, *>): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, component.name)

    fun loadResources(baseName: String): ResourceBundle? =
        FxUtil.loadResources(baseClass, locale, baseName)

    fun prompt(message: String): String =
        FxDialogs.prompt(message, this, stage)

    fun prompt(owner: Stage, message: String): String =
        FxDialogs.prompt(message, this, owner)

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
        contextBuilder.copyOf()
            .setRegistry(registry)
            .setBaseClass(baseClass)
            .setCharset(charset)
            .setLocale(locale)
            .also {
                if (skin != null) it.setSkin(skin)
            }

    fun <D : Any, I : D> register(name: String, declarationClass: KClass<D>, implementationClass: KClass<I>): Unit =
        registry.register(name, declarationClass, implementationClass)

    fun <D : Any, I : D> register(
        name: String,
        declarationClass: KClass<D>,
        implementationClass: KClass<I>,
        existingBean: I
    ): Unit = registry.register(name, declarationClass, implementationClass, existingBean)

    fun <T : Any> register(type: KClass<T>, existingBean: T): Unit =
        register(type.qualifiedName!!, type, type, existingBean)

    fun <T : Any> registerAndGet(type: KClass<T>): T {
        register(type.qualifiedName!!, type, type)
        return getBean(type)
    }

    internal fun initializeContext(hostServices: HostServices, parameters: Application.Parameters) {
        register(HostServices::class, hostServices)
        register(Application.Parameters::class, parameters)
    }

    override fun close() {}


    override fun toString(): String =
        "FXContextImpl{baseClass=${baseClass}, charset=${charset}, locale=${locale}, skin=${skin}}"

    companion object {
        private const val FACTORY = "java.util.prefs.PreferencesFactory"

        internal lateinit var contextBuilder: FxContextBuilder
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
            FxIntUtil.doPrivileged { System.setProperty(FACTORY, FxFilePreferencesFactory::class.qualifiedName!!) }
        }

        fun builder(): FxContextBuilder =
            if (::contextBuilder.isInitialized) contextBuilder.copyOf() else FxContextBuilder()

    }

    @Suppress("MemberVisibilityCanBePrivate")
    class FxContextBuilder internal constructor() {
        lateinit var baseClass: KClass<*>
            private set
        var charset: Charset = Charsets.UTF_8
            private set
        lateinit var locale: Locale
            private set
        var registry: FxRegistry = FxRegistry()
            private set
        var skin: String? = null
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
