package gargoyle.rpycg.convert;

import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.service.ModelConverter;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
class ModelConverterTest {
    private ModelConverter modelConverter;
    private ModelItem templateTree;
    @BeforeEach
    void setUp() {
        modelConverter = new ModelConverter();
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
    void toModelToTree() {
        TreeItem<DisplayItem> tree = modelConverter.toTree(templateTree);
        ModelItem actual = modelConverter.toModel(tree);
        assertEquals(templateTree, actual, "model test fail");
    }
}
