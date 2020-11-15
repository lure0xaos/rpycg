module RenPyCheatGenerator {
    requires javafx.graphics;
    requires javafx.controls;
    requires org.jetbrains.annotations;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.prefs;
    requires freemarker;
    requires java.desktop;

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
