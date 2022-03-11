package gargoyle.fx

import javafx.scene.Parent
import javafx.scene.control.Dialog
import javafx.stage.Stage
import java.net.URL
import java.util.ResourceBundle
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FxComponent<C, V : Parent> internal constructor(
    private val context: FxContext,
    val location: URL,
    val name: String,
    val controller: C,
    val view: V
) {

    fun findResource(baseClass: KClass<*>, resourceName: String, suffix: String): URL? =
        FxUtil.findResource(baseClass, context.locale, context.resolveBaseName(baseClass, resourceName), suffix)

    fun findResource(resourceName: String, suffix: String): URL? =
        FxUtil.findResource(context.baseClass, context.locale, context.resolveBaseName(name, resourceName), suffix)

    fun findResource(suffix: String): URL? =
        FxUtil.findResource(context.baseClass, context.locale, name, suffix)

    val primaryStage: Stage
        get() = context.stage
    val stage: Stage
        get() = requireNotNull(FxUtil.findStage(view)) { "no Stage" }

    fun <R, V2 : Parent> loadDialog(dialog: Dialog<R>): FxComponent<Dialog<R>, V2>? =
        FxIntUtil.loadDialog(context, dialog)

    fun loadResources(aClass: KClass<*>): ResourceBundle? =
        FxUtil.loadResources(context.baseClass, context.locale, resolveBaseName(aClass))

    fun loadResources(bundleName: String): ResourceBundle? =
        FxUtil.loadResources(context.baseClass, context.locale, bundleName)

    fun loadResources(): ResourceBundle? =
        FxUtil.loadResources(context.baseClass, context.locale, name)

    fun resolveBaseName(name: String): String =
        FxUtil.resolveBaseName(this.name, name)

    fun resolveBaseName(aClass: KClass<*>): String =
        FxUtil.resolveBaseName(aClass)

    fun resolveBaseName(aClass: KClass<*>, resourceName: String): String =
        FxUtil.resolveBaseName(aClass, resourceName)

    override fun toString(): String =
        "FXComponent{baseName=${name}, location=${location}}"
}
