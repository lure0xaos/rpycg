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

@Suppress("UNCHECKED_CAST")
internal object FxIntUtil {

    fun <C : Any, V : Parent> loadComponent(
        baseName: String,
        context: FxContext = FxContext.current,
        controller: C? = null,
        root: V? = null
    ): FxComponent<C, V>? =
        FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT_FXML)?.let {
            with(FXMLLoader()) {
                charset = context.charset
                classLoader = context.baseClass.java.classLoader
                controllerFactory = Callback { context.registerAndGet(it.kotlin) }
                FxUtil.loadResources(context.baseClass, context.locale, baseName)?.let { resources = it }
                location = it
                controller?.let { setController(it) }
                root?.let { setRoot(it) }
                try {
                    load<V>()
                } catch (e: IOException) {
                    FxLog.error("Error loading component $baseName", e)
                    null
                }?.let {
                    prepareView(baseName, context, it.sceneProperty(), FxUtil.findScene(it))
                    FxComponent(context, location, baseName, getController(), it)
                }
            }
        }

    fun <D : Dialog<*>, V : Parent> loadDialog(dialog: D, context: FxContext = FxContext.current): FxComponent<D, V>? =
        loadComponent<D, V>(FxUtil.resolveBaseName(dialog::class), context, dialog, null)
            ?.also { dialog.dialogPane.content = it.view }


    fun prepareStage(baseName: String, window: Window, context: FxContext = FxContext.current) {
        prepareView(baseName, context, window.sceneProperty(), FxUtil.findScene(window))
        FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT__IMAGES)?.toExternalForm()
            ?.let { if (window is Stage) window.icons += Image(it) }
    }

    private fun prepareView(
        baseName: String,
        context: FxContext = FxContext.current,
        sceneProperty: ObservableValue<Scene>? = null,
        scene: Scene? = null
    ) {
        sceneProperty?.addListener { _: ObservableValue<out Scene>, oldValue: Scene?, newValue: Scene? ->
            FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT_CSS)?.toExternalForm()
                ?.let {
                    oldValue?.stylesheets?.remove(it)
                    newValue?.stylesheets?.add(it)
                }
            context.skin?.let {
                FxUtil.findResource(context.baseClass, context.locale, "${baseName}_${it}", FxConstants.EXT_CSS)
            }?.toExternalForm()?.let {
                oldValue?.stylesheets?.remove(it)
                newValue?.stylesheets?.add(it)
            }
        }
        FxUtil.findResource(context.baseClass, context.locale, baseName, FxConstants.EXT_CSS)?.toExternalForm()
            ?.let { scene?.stylesheets?.add(it) }
        context.skin?.let {
            FxUtil.findResource(context.baseClass, context.locale, "${baseName}_${it}", FxConstants.EXT_CSS)
        }?.toExternalForm()?.let { scene?.stylesheets?.add(it) }
    }

    fun <T : Any> loadClass(className: String, classLoader: ClassLoader): KClass<T> =
        doPrivileged {
            try {
                Class.forName(className, false, classLoader)
            } catch (e: ClassNotFoundException) {
                error("could not load class $className")
            }
        }.kotlin as KClass<T>

    fun <T : Any> newInstance(type: KClass<T>): T =
        doPrivileged { type.constructors.first { it.parameters.isEmpty() }.call() }

    fun <T : Any?> doPrivileged(action: () -> T): T =
        try {
            AccessController.doPrivileged(PrivilegedExceptionAction(action) as PrivilegedExceptionAction<T>)
        } catch (e: PrivilegedActionException) {
            throw FxException("Privileged action error", e.cause?.cause)
        }
}
