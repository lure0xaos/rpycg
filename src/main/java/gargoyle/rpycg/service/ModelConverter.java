package gargoyle.rpycg.service;

import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Set;

public final class ModelConverter {

    @NotNull
    public ModelItem toModel(@NotNull TreeItem<DisplayItem> root) {
        DisplayItem rootValue = root.getValue();
        ModelItem parent = ModelItem.createMenu(rootValue.getLabel(), rootValue.getName());
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
                DisplayItem menu = DisplayItem.createMenu(item.getLabel(), item.getName());
                Set<ModelItem> children = item.getChildren();
                TreeItem<DisplayItem> treeItem = DisplayItem.toTreeItem(menu,
                        item.getParent() == null || children.isEmpty());
                for (ModelItem child : children) {
                    treeItem.getChildren().add(toTree(child));
                }
                return treeItem;
            case VARIABLE:
                DisplayItem variable = DisplayItem.createVariable(
                        item.getType() == null ? VarType.STR : item.getType(),
                        item.getLabel(), item.getName(), item.getValue()
                );
                return DisplayItem.toTreeItem(variable, false);
            default:
                throw new IllegalStateException(MessageFormat.format("unsupported model type {0}", modelType));
        }
    }
}
