package gargoyle.rpycg.model;

import gargoyle.rpycg.ui.model.FULLNESS;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public final class ModelTemplate {
    private ModelTemplate() {
        throw new IllegalStateException(getClass().getName());
    }

    @NotNull
    public static ModelItem getTemplateTree() {
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
        return rootMenu;
    }

    @NotNull
    public static ModelItem getTestTemplateTree() {
        ModelItem rootMenu = ModelItem.createMenu("", "");
        int v = 1;
        for (int i = 0; i < 2; i++) {
            rootMenu.addChild(createVariable(v));
            v++;
        }
        int m = 1;
        ModelItem menuAlmost = createMenu(m);
        m++;
        int almostSize = FULLNESS.ALMOST.getSize();
        for (int i = 0; i <= almostSize; i++) {
            menuAlmost.addChild(createVariable(v));
            v++;
        }
        rootMenu.addChild(menuAlmost);
        ModelItem menuFull = createMenu(m);
        int fullSize = FULLNESS.FULL.getSize();
        for (int i = 0; i <= fullSize; i++) {
            menuFull.addChild(createVariable(v));
            v++;
        }
        rootMenu.addChild(menuFull);
        for (int i = 0; i < 3; i++) {
            rootMenu.addChild(createVariable(v));
            v++;
        }
        return rootMenu;
    }

    @NotNull
    private static ModelItem createVariable(int v) {
        return ModelItem.createVariable(VarType.STR,
                MessageFormat.format("custom name{0}", v), MessageFormat.format("variable_name{0}", v), "");
    }

    @NotNull
    private static ModelItem createMenu(int m) {
        return ModelItem.createMenu(MessageFormat.format("menu_title{0}", m), MessageFormat.format("menu_title{0}", m));
    }
}
