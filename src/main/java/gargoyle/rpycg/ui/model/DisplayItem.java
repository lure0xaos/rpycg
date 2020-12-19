package gargoyle.rpycg.ui.model;

import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;

import java.text.MessageFormat;
import java.util.Objects;

public final class DisplayItem {
    private final Property<String> label;
    private final ReadOnlyProperty<ModelType> modelType;
    private final Property<String> name;
    private final Property<VarType> type;
    private final Property<String> value;

    private DisplayItem(ModelType modelType,
                        String label, String name, String value,
                        VarType type) {
        this.modelType = new SimpleObjectProperty<>(modelType);
        this.label = new SimpleStringProperty(label);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
        this.type = new SimpleObjectProperty<>(type);
    }

    public static DisplayItem createRoot() {
        return createMenu("", "");
    }

    public static DisplayItem createMenu(String label, String name) {
        return new DisplayItem(ModelType.MENU, label, name, "", null);
    }

    public static DisplayItem createVariable(VarType type,
                                             String label, String name, String value) {
        return new DisplayItem(ModelType.VARIABLE, label, name, value, type);
    }

    public static TreeItem<DisplayItem> toTreeItem(DisplayItem item, boolean expanded) {
        TreeItem<DisplayItem> treeItem = new DisplayTreeItem(item);
        treeItem.setExpanded(expanded);
        return treeItem;
    }

    public DisplayItem copyOf() {
        return new DisplayItem(getModelType(), getLabel(), getName(), getValue(), getType());
    }

    public ModelType getModelType() {
        return modelType.getValue();
    }

    public String getLabel() {
        return label.getValue();
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public String getValue() {
        return value.getValue();
    }

    public VarType getType() {
        return type.getValue();
    }

    public void setType(VarType type) {
        this.type.setValue(type);
    }

    public void setValue(String value) {
        this.value.setValue(value);
    }

    public void setLabel(String label) {
        this.label.setValue(label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || DisplayItem.class != obj.getClass()) {
            return false;
        }
        DisplayItem item = (DisplayItem) obj;
        return modelType.getValue() == item.modelType.getValue() && name.getValue().equals(item.name.getValue());
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[{1}]'{'type={2}, label={3}, name={4}, value={5}'}'",
                DisplayItem.class.getSimpleName(),
                modelType.getValue(), type.getValue(), label.getValue(),
                name.getValue(), value.getValue());
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
        private DisplayTreeItem(DisplayItem item) {
            super(item);
        }

        @Override
        public String toString() {
            return "TreeItem: " + getValue();
        }
    }
}
