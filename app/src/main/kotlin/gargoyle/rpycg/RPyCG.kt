package gargoyle.rpycg

import gargoyle.fx.FxLauncher.run
import gargoyle.rpycg.ui.RPyCGApp

object RPyCG {
    @JvmStatic
    fun main(args: Array<String>) = run(RPyCGApp::class, args)
}
