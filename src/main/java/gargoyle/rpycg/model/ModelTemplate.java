package gargoyle.rpycg.model;

import org.jetbrains.annotations.NotNull;

public final class ModelTemplate {
    private ModelTemplate() {
        throw new IllegalStateException(getClass().getName());
    }

    @NotNull
    public static ModelItem getTemplateTree() {
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
        return rootMenu;
    }

    @SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "DuplicatedCode", "UnusedAssignment"})
    @NotNull
    public static ModelItem getTestTemplateTree() {
        ModelItem rootMenu = ModelItem.createMenu("");
        int v = 1;
        rootMenu.addChild(createVariable(v++));
        rootMenu.addChild(createVariable(v++));
        int m = 1;
        ModelItem menu = createMenu(m++);
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        menu.addChild(createVariable(v++));
        rootMenu.addChild(menu);
        ModelItem menu2 = createMenu(m++);
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        menu2.addChild(createVariable(v++));
        rootMenu.addChild(menu2);
        rootMenu.addChild(createVariable(v++));
        rootMenu.addChild(createVariable(v++));
        rootMenu.addChild(createVariable(v++));
        return rootMenu;
    }

    @NotNull
    private static ModelItem createVariable(int v) {
        return ModelItem.createVariable(VarType.STR,
                String.format("custom name%d", v), String.format("variable_name%d", v), "");
    }

    @NotNull
    private static ModelItem createMenu(int m) {
        return ModelItem.createMenu(String.format("submenu_title%d", m));
    }
}
