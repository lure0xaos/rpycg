package gargoyle.rpycg

import gargoyle.fx.FxLauncher
import gargoyle.rpycg.ui.RPyCGApp

object RPyCG {
    @JvmStatic
    fun main(args: Array<String>): Unit = FxLauncher.run(RPyCGApp::class, args)
}
