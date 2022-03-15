module RenPyCheatGeneratorFX {
    exports gargoyle.fx;
    exports gargoyle.fx.log;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;
    requires java.logging;

    opens gargoyle.fx.icons;
}
