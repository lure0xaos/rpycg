module RenPyCheatGenerator {
    requires freemarker;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;
    requires org.jetbrains.annotations;
    requires org.slf4j;

    exports gargoyle.rpycg.fx to javafx.fxml, javafx.controls, javafx.graphics, java.prefs;
    exports gargoyle.rpycg.model to freemarker;
    exports gargoyle.rpycg.ui.model;

    opens gargoyle.rpycg;
    opens gargoyle.rpycg.model to javafx.base;
    opens gargoyle.rpycg.service;
    opens gargoyle.rpycg.ui.icons;
    opens gargoyle.rpycg.ui.flags;
    opens gargoyle.rpycg.ui.model to javafx.base;
    opens gargoyle.rpycg.ui;
}
