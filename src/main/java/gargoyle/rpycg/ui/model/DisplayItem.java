package gargoyle.rpycg.ui.model;

import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class DisplayItem {

    private final Property<String> label;
    private final ReadOnlyProperty<ModelType> modelType;
    private final Property<String> name;
    private final Property<VarType> type;
    private final Property<String> value;

    private DisplayItem(@NotNull ModelType modelType,
                        @NotNull String label, @NotNull String name, @NotNull String value,
                        @Nullable VarType type) {
        this.modelType = new SimpleObjectProperty<>(modelType);
        this.label = new SimpleStringProperty(label);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
        this.type = new SimpleObjectProperty<>(type);
    }

    @NotNull
    public static DisplayItem createRoot() {
        return createSubmenu("");
    }

    public static DisplayItem createSubmenu(@NotNull String label) {
        return new DisplayItem(ModelType.MENU, label, "", "", null);
    }

    public static DisplayItem createVariable(@NotNull VarType type,
                                             @NotNull String label, @NotNull String name, @NotNull String value) {
        return new DisplayItem(ModelType.VARIABLE, label, name, value, type);
    }

    @NotNull
    public static TreeItem<DisplayItem> toTreeItem(@NotNull DisplayItem item) {
        TreeItem<DisplayItem> treeItem = new DisplayTreeItem(item);
        treeItem.setExpanded(item.getModelType() == ModelType.MENU);
        return treeItem;
    }

    @NotNull
    public ModelType getModelType() {
        return modelType.getValue();
    }

    public DisplayItem copyOf() {
        return new DisplayItem(getModelType(), getLabel(), getName(), getValue(), getType());
    }

    @NotNull
    public String getLabel() {
        return label.getValue();
    }

    @NotNull
    public String getName() {
        return name.getValue();
    }

    @NotNull
    public String getValue() {
        return value.getValue();
    }

    @NotNull
    public VarType getType() {
        return type.getValue();
    }

    public void setType(@Nullable VarType type) {
        this.type.setValue(type);
    }

    public void setValue(@NotNull String value) {
        this.value.setValue(value);
    }

    public void setName(@NotNull String name) {
        this.name.setValue(name);
    }

    public void setLabel(@NotNull String label) {
        this.label.setValue(label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType, label, name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DisplayItem item = (DisplayItem) obj;
        return modelType.equals(item.modelType) &&
                label.equals(item.label) &&
                name.equals(item.name) &&
                value.equals(item.value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]{type=%s, label=%s, name=%s, value=%s}", getClass().getSimpleName(),
                modelType.getValue(), type.getValue(), label.getValue(),
                name.getValue(), value.getValue());
    }

    @NotNull
    public Property<String> labelProperty() {
        return label;
    }

    @NotNull
    public ReadOnlyProperty<ModelType> modelTypeProperty() {
        return modelType;
    }

    @NotNull
    public Property<String> nameProperty() {
        return name;
    }

    @NotNull
    public Property<VarType> typeProperty() {
        return type;
    }

    @NotNull
    public Property<String> valueProperty() {
        return value;
    }

    private static final class DisplayTreeItem extends TreeItem<DisplayItem> {
        private DisplayTreeItem(@NotNull DisplayItem item) {
            super(item);
        }

        @Override
        public String toString() {
            return "TreeItem: " + getValue();
        }
    }
}
