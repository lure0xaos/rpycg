package gargoyle.rpycg;

import gargoyle.fx.FXLauncher;
import gargoyle.rpycg.ui.RPyCGApp;

public final class RPyCG {

    private RPyCG() {
    }

    public static void main(final String[] args) {
        FXLauncher.run(RPyCGApp.class, args);
    }
}
