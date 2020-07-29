module RenPyCheatGenerator {
    requires javafx.graphics;
    requires javafx.controls;
    requires org.jetbrains.annotations;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.prefs;
    requires freemarker;
    requires java.desktop;

    exports gargoyle.rpycg.ui to javafx.fxml, javafx.controls, javafx.graphics;
    exports gargoyle.rpycg.model to freemarker;
    opens gargoyle.rpycg.ui.icons;
    opens gargoyle.rpycg.model to javafx.base;
    opens gargoyle.rpycg.ui.model to javafx.base;
    opens gargoyle.rpycg.ui;
    opens gargoyle.rpycg.service;
    opens gargoyle.rpycg;
}
