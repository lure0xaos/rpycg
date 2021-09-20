package gargoyle.rpycg.service;

import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.scene.control.TreeItem;

import java.text.MessageFormat;
import java.util.Set;

public final class ModelConverter {
    public ModelItem toModel(final TreeItem<DisplayItem> root) {
        final DisplayItem rootValue = root.getValue();
        final ModelItem parent = ModelItem.createMenu(rootValue.getLabel(), rootValue.getName());
        for (final TreeItem<DisplayItem> treeItem : root.getChildren()) {
            final DisplayItem value = treeItem.getValue();
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

    public TreeItem<DisplayItem> toTree(final ModelItem item) {
        final ModelType modelType = item.getModelType();
        switch (modelType) {
            case MENU:
                final DisplayItem menu = DisplayItem.createMenu(item.getLabel(), item.getName());
                final Set<ModelItem> children = item.getChildren();
                final TreeItem<DisplayItem> treeItem = DisplayItem.toTreeItem(menu,
                        null == item.getParent() || children.isEmpty());
                for (final ModelItem child : children) {
                    treeItem.getChildren().add(toTree(child));
                }
                return treeItem;
            case VARIABLE:
                final DisplayItem variable = DisplayItem.createVariable(
                        null == item.getType() ? VarType.STR : item.getType(),
                        item.getLabel(), item.getName(), item.getValue()
                );
                return DisplayItem.toTreeItem(variable, false);
            default:
                throw new IllegalStateException(MessageFormat.format("unsupported model type {0}", modelType));
        }
    }
}
