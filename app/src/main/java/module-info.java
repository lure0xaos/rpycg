module RenPyCheatGenerator {
    requires RenPyCheatGeneratorFX;
    requires java.desktop;
    requires java.prefs;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;

    exports gargoyle.rpycg.ui to RenPyCheatGeneratorFX, javafx.fxml;
    opens gargoyle.rpycg.util;
    opens gargoyle.rpycg.ex;
    opens gargoyle.rpycg.service;
    opens gargoyle.rpycg.ui;
    opens gargoyle.rpycg.ui.model;
    opens gargoyle.rpycg.ui.flags;
    opens gargoyle.rpycg.ui.icons;
}
