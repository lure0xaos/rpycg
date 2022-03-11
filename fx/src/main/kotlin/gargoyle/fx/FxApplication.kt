package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import javafx.scene.Parent

@Suppress("unused")
fun interface FxApplication {

    fun doInit() {}

    fun doStart(): FxComponent<*, out Parent>

    fun doStop() {}

    companion object {
        fun notifySplash(splashNotification: FxSplashNotification) {
            FxLauncher.notifySplash(splashNotification)
        }
    }
}
