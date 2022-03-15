package gargoyle.rpycg.ui.model;

import gargoyle.fx.FXUtil;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;

import java.util.Map;
import java.util.Objects;

public final class DisplayItem {
    private final Property<String> label;
    private final ReadOnlyProperty<ModelType> modelType;
    private final Property<String> name;
    private final Property<VarType> type;
    private final Property<String> value;

    private DisplayItem(final ModelType modelType,
                        final String label, final String name, final String value,
                        final VarType type) {
        this.modelType = new SimpleObjectProperty<>(modelType);
        this.label = new SimpleStringProperty(label);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
        this.type = new SimpleObjectProperty<>(type);
    }

    public static DisplayItem createMenu(final String label, final String name) {
        return new DisplayItem(ModelType.MENU, label, name, "", null);
    }

    public static DisplayItem createRoot() {
        return createMenu("", "");
    }

    public static DisplayItem createVariable(final VarType type,
                                             final String label, final String name, final String value) {
        return new DisplayItem(ModelType.VARIABLE, label, name, value, type);
    }

    public static TreeItem<DisplayItem> toTreeItem(final DisplayItem item, final boolean expanded) {
        final TreeItem<DisplayItem> treeItem = new DisplayTreeItem(item);
        treeItem.setExpanded(expanded);
        return treeItem;
    }

    public DisplayItem copyOf() {
        return new DisplayItem(getModelType(), getLabel(), getName(), getValue(), getType());
    }

    public String getLabel() {
        return label.getValue();
    }

    public void setLabel(final String label) {
        this.label.setValue(label);
    }

    public ModelType getModelType() {
        return modelType.getValue();
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(final String name) {
        this.name.setValue(name);
    }

    public VarType getType() {
        return type.getValue();
    }

    public void setType(final VarType type) {
        this.type.setValue(type);
    }

    public String getValue() {
        return value.getValue();
    }

    public void setValue(final String value) {
        this.value.setValue(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType, name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || DisplayItem.class != obj.getClass()) {
            return false;
        }
        final DisplayItem item = (DisplayItem) obj;
        return modelType.getValue() == item.modelType.getValue() && name.getValue().equals(item.name.getValue());
    }

    @Override
    public String toString() {
        return FXUtil.format("DisplayItem[{modelType}]{type={type}, label={label}, name={name}, value={value}}",
                Map.of(
                        "modelType", modelType,
                        "type", type,
                        "label", label,
                        "name", name,
                        "value", value
                ));
    }

    public Property<String> labelProperty() {
        return label;
    }

    public ReadOnlyProperty<ModelType> modelTypeProperty() {
        return modelType;
    }

    public Property<String> nameProperty() {
        return name;
    }

    public Property<VarType> typeProperty() {
        return type;
    }

    public Property<String> valueProperty() {
        return value;
    }

    private static final class DisplayTreeItem extends TreeItem<DisplayItem> {
        private DisplayTreeItem(final DisplayItem item) {
            super(item);
        }

        @Override
        public String toString() {
            return "TreeItem: " + getValue();
        }
    }
}
