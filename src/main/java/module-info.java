module RenPyCheatGenerator {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;
    requires java.logging;

    exports gargoyle.rpycg.fx to javafx.graphics, java.prefs;
    exports gargoyle.rpycg.ui to javafx.fxml;
    exports gargoyle.rpycg.ui.model to javafx.fxml;
    exports gargoyle.rpycg.model to javafx.fxml;
    exports gargoyle.rpycg.service to javafx.fxml;

    opens gargoyle.rpycg.ui to javafx.fxml;
    opens gargoyle.rpycg.fx.log to java.logging;
    exports gargoyle.rpycg.fx.log to java.prefs, javafx.graphics;
}
