@file:Suppress("DEPRECATION")

package gargoyle.fx

import gargoyle.fx.log.FxLog
import javafx.beans.value.ObservableValue
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Dialog
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Callback
import java.io.IOException
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "unused")
internal object FxIntUtil {

    fun <T : Any> loadClass(className: String, classLoader: ClassLoader): KClass<T> {
        return try {
            Class.forName(className, false, classLoader).kotlin as KClass<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("could not load class $className", e)
        }
    }

    fun <C : Any, V : Parent> loadComponent(
        context: FxContext,
        baseName: String,
        controller: C? = null,
        root: V? = null
    ): FxComponent<C, V>? {
        return FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT_FXML)
            ?.let { resource ->
                val fxmlLoader = FXMLLoader()
                fxmlLoader.charset = context.charset
                fxmlLoader.classLoader = context.baseClass.java.classLoader
                fxmlLoader.controllerFactory = Callback { type: Class<*> -> context.registerAndGet(type.kotlin) }
                FxUtil.loadResources(context.baseClass, context.locale, baseName)?.let { fxmlLoader.resources = it }
                fxmlLoader.location = resource
                controller?.let { fxmlLoader.setController(it) }
                root?.let { fxmlLoader.setRoot(it) }
                try {
                    fxmlLoader.load<V>()
                } catch (e: IOException) {
                    FxLog.error(e, "Error loading component $baseName")
                    null
                }?.let {
                    val view: V = fxmlLoader.getRoot()
                    prepareView(context, baseName, view.sceneProperty(), FxUtil.findScene(view))
                    FxComponent(context, fxmlLoader.location, baseName, fxmlLoader.getController(), view)
                }
            }
    }

    fun <D : Dialog<*>, V : Parent> loadDialog(context: FxContext, dialog: D): FxComponent<D, V>? =
        loadComponent<D, V>(context, FxUtil.resolveBaseName(dialog::class), dialog, null)
            ?.also { content: FxComponent<D, V> -> dialog.dialogPane.content = content.view }

    fun <T : Any> newInstance(type: KClass<T>): T =
        try {
            AccessController.doPrivileged(PrivilegedExceptionAction {
                type.constructors.first { it.parameters.isEmpty() }.call()
            } as PrivilegedExceptionAction<T>)
        } catch (e: PrivilegedActionException) {
            throw FxException("$type instantiation error", e.cause?.cause)
        }

    fun prepareStage(context: FxContext, baseName: String, window: Window) {
        prepareView(context, baseName, window.sceneProperty(), FxUtil.findScene(window))
        FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT__IMAGES)?.toExternalForm()
            ?.let { if (window is Stage) window.icons += Image(it) }
    }

    private fun prepareView(
        context: FxContext,
        baseName: String,
        sceneProperty: ObservableValue<Scene>,
        scene: Scene?
    ) {
        sceneProperty.addListener { _: ObservableValue<out Scene>, oldValue: Scene?, newValue: Scene ->
            FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT_CSS)?.toExternalForm()
                ?.let {
                    oldValue?.stylesheets?.remove(it)
                    newValue.stylesheets?.add(it)
                }
            context.skin.let {
                FxUtil.findResource(context.baseClass, context.locale, "${baseName}_${it}", FxConstants.EXT_CSS)
            }?.toExternalForm()?.let {
                oldValue?.stylesheets?.remove(it)
                newValue.stylesheets?.add(it)
            }
        }
        FxUtil.findResource(
            context.baseClass,
            context.locale,
            baseName,
            FxConstants.EXT_CSS
        )?.toExternalForm()?.let { scene?.stylesheets?.add(it) }
        context.skin.let {
            FxUtil.findResource(context.baseClass, context.locale, "${baseName}_${it}", FxConstants.EXT_CSS)
        }?.toExternalForm()?.let { scene?.stylesheets?.add(it) }
    }

}
