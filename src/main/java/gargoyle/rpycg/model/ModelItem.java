package gargoyle.rpycg.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModelItem {
    @NotNull
    private final Set<ModelItem> children = new LinkedHashSet<>();
    @NotNull
    private final String label;
    @NotNull
    private final ModelType modelType;
    @NotNull
    private final String name;
    @Nullable
    private final VarType type;
    @NotNull
    private final String value;
    @Nullable
    private ModelItem parent;

    private ModelItem(@NotNull ModelType modelType, @NotNull String label,
                      @NotNull String name, @NotNull String value, @Nullable VarType type) {
        this.modelType = modelType;
        this.label = label;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @NotNull
    public static ModelItem createMenu(@NotNull String label) {
        return new ModelItem(ModelType.MENU, label, label, "", null);
    }

    @NotNull
    public static ModelItem createVariable(@NotNull VarType type,
                                           @NotNull String label, @NotNull String name, @NotNull String value) {
        return new ModelItem(ModelType.VARIABLE, label, name, value, Objects.requireNonNull(type));
    }

    public void addChild(@NotNull ModelItem child) {
        children.add(child);
        child.parent = this;
    }

    public Set<ModelItem> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public ModelType getModelType() {
        return modelType;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public ModelItem getParent() {
        return parent;
    }

    @Nullable
    public VarType getType() {
        return type;
    }

    @NotNull
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, modelType, label, name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModelItem modelItem = (ModelItem) obj;
        return modelType == modelItem.modelType &&
               Objects.equals(parent, modelItem.parent) &&
               label.equals(modelItem.label) &&
               name.equals(modelItem.name) &&
               value.equals(modelItem.value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]{type=%s, label=%s, name=%s, value=%s,parent=%s}", getClass().getSimpleName(),
                modelType, type, label, name, value, Optional.ofNullable(parent).map(item -> item.label).orElse(""));
    }
}
