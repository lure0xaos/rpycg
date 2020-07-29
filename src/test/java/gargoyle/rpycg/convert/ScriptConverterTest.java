package gargoyle.rpycg.convert;

import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.service.ScriptConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
class ScriptConverterTest {
    private static final String TEMPLATE_SCRIPT = "template_script.txt";
    private ScriptConverter scriptConverter;
    private ModelItem templateTree;
    @Test
    void fromScript() throws Exception {
        ModelItem actual = scriptConverter.fromScript(Files.readAllLines(getPath(TEMPLATE_SCRIPT)));
        assertEquals(templateTree, actual, "wrong tree");
    }
    private Path getPath(String name) throws URISyntaxException {
        return Paths.get(getResource(name).toURI());
    }
    private static URL getResource(String name) {
        return FXUtil.requireNonNull(ScriptConverterTest.class.getClassLoader().getResource(name),
                () -> MessageFormat.format("no {0} found", name));
    }
    @BeforeEach
    void setUp() {
        scriptConverter = new ScriptConverter();
        ModelItem rootMenu = ModelItem.createMenu("", "");
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "custom name", "variable_name1", ""));
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name2", "variable_name2", ""));
        ModelItem menu = ModelItem.createMenu("menu_title", "menu_title");
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name3", "variable_name3", ""));
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name4", "variable_name4", ""));
        rootMenu.addChild(menu);
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name5", "variable_name5", ""));
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "fixed_variable6", "fixed_variable6", "100"));
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "custom name", "fixed_variable7", "100"));
        templateTree = rootMenu;
    }
    @Test
    void toScript() throws Exception {
        List<String> script = scriptConverter.toScript(templateTree);
        List<String> expected = Files.readAllLines(getPath(TEMPLATE_SCRIPT));
        assertEquals(String.join("\n", expected), String.join("\n", script), "wrong script");
    }
}
