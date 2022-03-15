package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import gargoyle.fx.log.FxLog
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Window
import java.net.URL
import java.util.ResourceBundle
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JWindow

class FxImageSplash : FxSplash {
    override fun createWindow(location: URL, resources: ResourceBundle?): Window =
        JWindow().also { window ->
            with(window.contentPane.also { it.layout = BorderLayout() }) {
                add(JLabel(ImageIcon(location)).also { background = TRANSPARENT }, BorderLayout.CENTER)
            }
        }.also { it.background = TRANSPARENT }

    override fun handleSplashNotification(splashNotification: FxSplashNotification): Unit =
        FxLog.info("${splashNotification.javaClass.simpleName}.${splashNotification.type}:${splashNotification.progress}(${splashNotification.details})")

    companion object {
        private val TRANSPARENT = Color(0, 0, 0, 0)
    }
}
