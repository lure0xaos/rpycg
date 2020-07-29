package gargoyle.rpycg.service;

import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;

public final class ModelConverter {

    @NotNull
    public ModelItem toModel(@NotNull TreeItem<DisplayItem> root) {
        ModelItem parent = ModelItem.createMenu(root.getValue().getLabel());
        for (TreeItem<DisplayItem> treeItem : root.getChildren()) {
            DisplayItem value = treeItem.getValue();
            switch (value.getModelType()) {
                case MENU:
                    parent.addChild(toModel(treeItem));
                    break;
                case VARIABLE:
                    parent.addChild(ModelItem.createVariable(
                            value.getType(), value.getLabel(), value.getName(),
                            value.getValue()
                    ));
            }
        }
        return parent;
    }

    @NotNull
    public TreeItem<DisplayItem> toTree(@NotNull ModelItem item) {
        ModelType modelType = item.getModelType();
        switch (modelType) {
            case MENU:
                TreeItem<DisplayItem> treeItem = DisplayItem.toTreeItem(DisplayItem.createSubmenu(item.getLabel()));
                for (ModelItem child : item.getChildren()) {
                    treeItem.getChildren().add(toTree(child));
                }
                return treeItem;
            case VARIABLE:
                return DisplayItem.toTreeItem(DisplayItem.createVariable(
                        item.getType() == null ? VarType.STR : item.getType(),
                        item.getLabel(), item.getName(), item.getValue()
                ));
            default:
                throw new IllegalStateException(String.format("unsupported model type %s", modelType));
        }
    }
}
