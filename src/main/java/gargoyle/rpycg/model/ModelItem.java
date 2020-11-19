package gargoyle.rpycg.model;

import gargoyle.rpycg.ui.model.FULLNESS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModelItem {
    @NotNull
    private final Set<ModelItem> children = new LinkedHashSet<>(FULLNESS.FULL.getSize());
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
    public static ModelItem createMenu(@NotNull String label, @NotNull String name) {
        return new ModelItem(ModelType.MENU, label, name, "", null);
    }

    @NotNull
    public static ModelItem createVariable(@Nullable VarType type,
                                           @NotNull String label, @NotNull String name, @NotNull String value) {
        return new ModelItem(ModelType.VARIABLE, label, name, value, type);
    }

    public void addChild(@NotNull ModelItem child) {
        children.add(child);
        child.parent = this;
    }

    @NotNull
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
        return Objects.hash(modelType, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || ModelItem.class != obj.getClass()) {
            return false;
        }
        ModelItem modelItem = (ModelItem) obj;
        return modelType == modelItem.modelType && name.equals(modelItem.name);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[{1}]'{'type={2}, label={3}, name={4}, value={5},parent={6}'}'",
                ModelItem.class.getSimpleName(),
                modelType, type, label, name, value, Optional.ofNullable(parent).map(item -> item.label)
                        .orElse(""));
    }
}
