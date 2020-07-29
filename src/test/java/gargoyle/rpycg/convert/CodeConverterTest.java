package gargoyle.rpycg.convert;

import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.Settings;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.service.CodeConverter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeConverterTest {
    private static final String TEMPLATE_CODE = "template_code.rpy";
    private CodeConverter codeConverter;
    private ModelItem templateTree;

    @BeforeEach
    void setUp() {
        codeConverter = new CodeConverter(
                FXContextFactory.currentContext(),
                new Settings(true, true, true, true, true,
                        new KeyCodeCombination(KeyCode.K, KeyCombination.SHIFT_DOWN),
                        new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN),
                        new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN),
                        new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN)),
                CodeConverter.SPACES);
        ModelItem rootMenu = ModelItem.createMenu("");
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "custom name", "variable_name1", ""));
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name2", "variable_name2", ""));
        ModelItem menu = ModelItem.createMenu("submenu_title");
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name3", "variable_name3", ""));
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name4", "variable_name4", ""));
        rootMenu.addChild(menu);
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name5", "variable_name5", ""));
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "fixed_variable6", "fixed_variable6", "100"));
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "custom name", "fixed_variable7", "100"));
        templateTree = rootMenu;
    }

    @Test
    void toCode() throws Exception {
        List<String> script = codeConverter.toCode(templateTree);
        List<String> expected = Files.readAllLines(Paths.get(getResource(TEMPLATE_CODE).toURI()));
        assertEquals(String.join("\n", expected), String.join("\n", script), "wrong script");
    }

    @NotNull
    private static URL getResource(String name) {
        return Objects.requireNonNull(CodeConverterTest.class.getClassLoader().getResource(name), () ->
                String.format("no %s found", name));
    }
}
