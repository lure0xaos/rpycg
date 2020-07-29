package gargoyle.rpycg;

import gargoyle.rpycg.ui.RPyCGApp;

public final class RPyCG {
    private RPyCG() {
    }

    @SuppressWarnings("AccessOfSystemProperties")
    public static void main(String[] args) {
        System.setProperty("javafx.sg.warn", "true");
        RPyCGApp.run(args);
    }
}
