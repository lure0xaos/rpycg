module RenPyCheatGeneratorFX {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.reflect;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;
    requires java.logging;

    exports gargoyle.fx;
    exports gargoyle.fx.log;
    opens gargoyle.fx.icons;
    opens gargoyle.fx.log.jul;
}
