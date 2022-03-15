package gargoyle.fx

import java.awt.Window
import java.net.URL
import java.util.ResourceBundle

fun interface FxSplash {
    fun createWindow(location: URL, resources: ResourceBundle?): Window
    fun handleSplashNotification(splashNotification: FxSplashNotification) {}
    interface FxSplashNotification {
        val details: String
        val progress: Double
        val type: Type

        enum class Type {
            PRE_INIT, INIT, START, STOP
        }
    }
}
