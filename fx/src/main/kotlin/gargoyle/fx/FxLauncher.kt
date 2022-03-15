package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import gargoyle.fx.log.FxLog
import gargoyle.fx.log.FxLoggerFactory
import gargoyle.fx.log.jul.FxJULLogger
import javafx.application.Application
import javafx.event.Event
import javafx.stage.WindowEvent
import java.awt.Window
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
object FxLauncher {

    class FxSplashNotificationImpl(
        override val type: FxSplashNotification.Type,
        override val progress: Double,
        override val details: String
    ) : FxSplashNotification

    val KEY_APP: String = FxWrapper::class.qualifiedName!!
    private lateinit var splash: FxSplash
    private lateinit var splashWindow: Window

    init {
        FxLoggerFactory.configure(FxJULLogger, FxJULLogger::class.java.getResource("logging.properties"))
    }


    fun exit(context: FxContext): Unit =
        Event.fireEvent(context.stage, WindowEvent(context.stage, WindowEvent.WINDOW_CLOSE_REQUEST))

    fun notifySplash(type: FxSplashNotification.Type, progress: Double, details: String): Unit =
        notifySplash(FxSplashNotificationImpl(type, progress, details))

    fun notifySplash(splashNotification: FxSplashNotification) {
        splash.handleSplashNotification(splashNotification)
    }

    fun requestPrevent(context: FxContext, callback: (FxContext) -> FxCloseAction) {
        FxCloseFlag.PREVENT[context] = callback
    }

    fun requestRestart(context: FxContext) {
        FxCloseFlag.RESTART[context] = { it: FxContext ->
            with(it) {
                FxCloseFlag.PREVENT.doIf(it)
                    .let { closeAction ->
                        try {
                            (this.stage.properties[KEY_APP] as FxWrapper?)?.let {
                                with(it) {
                                    askStop()
                                    init()
                                    start(stage)
                                }
                            }
                            when (closeAction) {
                                FxCloseAction.KEEP -> FxCloseAction.CLOSE
                                else -> FxCloseAction.KEEP
                            }
                        } catch (e: Exception) {
                            FxLog.error("{location} initialization error", e)
                            FxCloseAction.CLOSE
                        }
                    }

            }
        }
        exit(context)
    }

    fun run(appClass: KClass<out FxApplication>, args: Array<String>): Unit =
        try {
            FxContext.contextBuilder = FxContext.builder().setBaseClass(appClass).setLocale(Locale.getDefault())
            splashStart(appClass)
            Application.launch(FxWrapper::class.java, *arrayOf(appClass.qualifiedName!!) + args)
        } catch (e: Exception) {
            FxLog.error(FxConstants.KEY_SPLASH, e)
            splashStop("")
        }

    fun splashStop(details: String) {
        if (::splashWindow.isInitialized) {
            splashWindow.let {
                if (it.isVisible) {
                    notifySplash(FxSplashNotification.Type.STOP, 1.0, details)
                    it.dispose()
                }
            }
        }
    }

    private fun splashStart(appClass: KClass<out FxApplication>) {
        val splashClassName = FxIntUtil.doPrivileged<String> {
            System.getProperty(FxConstants.KEY_SPLASH_CLASS, FxConstants.SPLASH_CLASS_DEFAULT)
        }
        try {
            val splashClass: KClass<out FxSplash> = FxIntUtil.loadClass(splashClassName, appClass.java.classLoader)
            splash = FxIntUtil.newInstance(splashClass)
                .also { fxSplash ->
                    with(
                        fxSplash.createWindow(FxIntUtil.doPrivileged<String?> { System.getProperty(FxConstants.KEY_SPLASH) }
                            ?.let { appClass.java.getResource(it) }
                            ?: FxWrapper::class.java.getResource(FxConstants.SPLASH_DEFAULT),
                            ResourceBundle.getBundle(
                                splashClass.qualifiedName!!.replace('.', '/'),
                                Locale.getDefault(),
                                splashClass.java.classLoader
                            )
                        )
                    ) {
                        pack()
                        isAlwaysOnTop = true
                        setLocationRelativeTo(null)
                        isVisible = true
                        splashWindow = this
                    }
                }
            splash.also { notifySplash(FxSplashNotification.Type.PRE_INIT, 0.0, "") }
        } catch (e: MissingResourceException) {
            FxLog.warn("No resources $splashClassName")
        } catch (e: FxException) {
            FxLog.warn("Other error from $splashClassName")
        }
    }

}
