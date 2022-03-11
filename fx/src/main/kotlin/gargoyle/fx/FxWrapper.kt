package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import gargoyle.fx.log.FxLog
import javafx.application.Application
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.awt.Window
import kotlin.reflect.KClass

class FxWrapper : Application() {
    private lateinit var appClass: KClass<out FxApplication>
    private lateinit var appClassName: String
    private lateinit var context: FxContext

    @Suppress("UNCHECKED_CAST")
    override fun init() {
        try {
            appClassName = parameters.raw[0]
            appClass = FxIntUtil.loadClass(appClassName, javaClass.classLoader)
            context = FxContext.current
            context.register(appClassName, appClass as KClass<FxApplication>, appClass as KClass<FxApplication>)
            context.initializeContext(hostServices, parameters)
            FxLauncher.notifySplash(FxSplashNotification.Type.INIT, 0.0, "")
            val application = context.getBean(appClassName, appClass)
            application.doInit()
        } catch (e: Exception) {
            FxLauncher.splashStop("")
            FxLog.error(e, "$appClassName initialization error")
            throw FxUserException(e, "$appClassName initialization error")
        }
    }

    override fun start(primaryStage: Stage) = try {
        context.register(Stage::class, primaryStage)
        primaryStage.properties[FxLauncher.KEY_APP] = this
        primaryStage.onCloseRequest = EventHandler { event: WindowEvent ->
            try {
                if (FxCloseFlag.values()
                        .any { closeFlag: FxCloseFlag -> FxCloseAction.KEEP == closeFlag.doIf(context) }
                ) {
                    event.consume()
                }
            } catch (e: Exception) {
                FxLog.error(e, e.localizedMessage)
            }
        }
        FxLauncher.notifySplash(FxSplashNotification.Type.START, 0.0, "")
        FxIntUtil.prepareStage(context, FxUtil.resolveBaseName(appClass), primaryStage)
        FxUtil.loadResources(context.baseClass, context.locale, FxUtil.resolveBaseName(appClass))
            ?.let { primaryStage.title = it.getString(LC_TITLE) }
        primaryStage.scene = FxUtil.getOrCreateScene(context.getBean(appClassName, appClass).doStart().view)
        primaryStage.centerOnScreen()
        primaryStage.show()
        FxLauncher.splashStop("")
    } catch (e: Exception) {
        FxLauncher.splashStop("$appClassName initialization error")
        FxLog.error(e, "$appClassName initialization error")
        throw FxUserException(e, "$appClassName initialization error")
    }

    override fun stop() {
        try {
            askStop()
            Window.getWindows().filter { obj: Window -> obj.isDisplayable }.forEach { obj: Window -> obj.dispose() }
        } catch (e: Exception) {
            FxLog.error(e, "$appClassName initialization error")
            throw FxUserException(e, "$appClassName initialization error")
        }
    }

    fun askStop() {
        try {
            context.getBean(appClassName, appClass).doStop()
        } finally {
            FxLauncher.splashStop("$appClassName initialization error")
            context.registry.close()
        }
    }

    companion object {
        private const val LC_TITLE = "title"
    }
}
