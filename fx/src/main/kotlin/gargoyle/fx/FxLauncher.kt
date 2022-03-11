@file:Suppress("DEPRECATION")

package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import gargoyle.fx.log.FxLog
import javafx.application.Application
import javafx.event.Event
import javafx.stage.WindowEvent
import javafx.util.Callback
import java.awt.Window
import java.io.IOException
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import java.util.logging.LogManager
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FxLauncher {

    class FxSplashNotificationImpl(
        override val type: FxSplashNotification.Type,
        override val progress: Double,
        override val details: String
    ) : FxSplashNotification

    val KEY_APP: String = FxWrapper::class.qualifiedName!!
    private var splash: FxSplash? = null
    private var splashWindow: Window? = null

    init {
        try {
            LogManager.getLogManager()
                .readConfiguration(FxLauncher::class.java.getResourceAsStream("logging.properties"))
        } catch (e: IOException) {
            FxLog.error(e, "{location} initialization error")
        }
    }

    fun exit(context: FxContext) {
        val primaryStage = context.stage
        Event.fireEvent(primaryStage, WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST))
    }

    fun notifySplash(
        type: FxSplashNotification.Type,
        progress: Double, details: String
    ) {
        notifySplash(FxSplashNotificationImpl(type, progress, details))
    }

    fun notifySplash(splashNotification: FxSplashNotification) {
        if (null != splash) {
            splash!!.handleSplashNotification(splashNotification)
        }
    }

    fun requestPrevent(context: FxContext, callback: Callback<FxContext, FxCloseAction>) {
        FxCloseFlag.PREVENT[context] = callback
    }

    fun requestRestart(context: FxContext) {
        FxCloseFlag.RESTART[context] = { fxContext: FxContext ->
            val saved = FxCloseFlag.PREVENT.doIf(fxContext)
            try {
                val stage = fxContext.stage
                val wrapper = getWrapper(fxContext)
                wrapper!!.askStop()
                wrapper.init()
                wrapper.start(stage)
            } catch (e: Exception) {
                FxLog.error(
                    e, "{location} initialization error"
                )
                FxCloseAction.CLOSE
            }
            if (FxCloseAction.KEEP == saved) FxCloseAction.CLOSE else FxCloseAction.KEEP
        }
        exit(context)
    }

    fun run(appClass: KClass<out FxApplication>, args: Array<String>) {
        try {
            FxContext.builder0 = FxContext.builder().setBaseClass(appClass).setLocale(Locale.getDefault())
            splashStart(appClass)
            val newArgs = arrayOfNulls<String>(args.size + 1)
            System.arraycopy(args, 0, newArgs, 1, args.size)
            newArgs[0] = appClass.qualifiedName
            Application.launch(FxWrapper::class.java, *newArgs)
        } catch (e: Exception) {
            FxLog.error(e, FxConstants.KEY_SPLASH)
            splashStop("")
        }
    }

    fun splashStop(details: String) {
        if (null != splashWindow) {
            if (splashWindow!!.isVisible) {
                notifySplash(FxSplashNotification.Type.STOP, 1.0, details)
                splashWindow!!.dispose()
            }
        }
    }

    private fun getWrapper(fxContext: FxContext): FxWrapper? = fxContext.stage.properties[KEY_APP] as FxWrapper?

    private fun splashStart(appClass: KClass<out FxApplication>) {
        val splashClassName = AccessController.doPrivileged(PrivilegedAction {
            System.getProperty(
                FxConstants.KEY_SPLASH_CLASS,
                FxConstants.SPLASH_CLASS_DEFAULT
            )
        } as PrivilegedAction<String>)
        try {
            val splashClass: KClass<out FxSplash> =
                FxIntUtil.loadClass(splashClassName, appClass.java.classLoader)
            splash = FxIntUtil.newInstance(splashClass)
            val splashLocation =
                AccessController.doPrivileged(PrivilegedAction { System.getProperty(FxConstants.KEY_SPLASH) } as PrivilegedAction<String>)
            splashWindow = splash!!.createWindow(
                if (null != splashLocation) appClass.java.getResource(splashLocation) else FxWrapper::class.java.getResource(
                    FxConstants.SPLASH_DEFAULT
                ),
                ResourceBundle.getBundle(FxUtil.resolveBaseName(splashClass))
            )
            splashWindow!!.pack()
            splashWindow!!.isAlwaysOnTop = true
            splashWindow!!.setLocationRelativeTo(null)
            splashWindow!!.isVisible = true
            notifySplash(FxSplashNotification.Type.PRE_INIT, 0.0, "")
        } catch (e: MissingResourceException) {
            FxLog.warn("No resources $splashClassName")
        } catch (e: FxException) {
            FxLog.warn("No resources $splashClassName")
        }
    }

}
