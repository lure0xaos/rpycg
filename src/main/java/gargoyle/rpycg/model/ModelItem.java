package gargoyle.rpycg.model;

import gargoyle.rpycg.ui.model.FULLNESS;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModelItem {
    private final Set<ModelItem> children = new LinkedHashSet<>(FULLNESS.FULL.getSize());
    private final String label;
    private final ModelType modelType;
    private final String name;
    private final VarType type;
    private final String value;
    private ModelItem parent;

    private ModelItem(ModelType modelType, String label,
                      String name, String value, VarType type) {
        this.modelType = modelType;
        this.label = label;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public static ModelItem createMenu(String label, String name) {
        return new ModelItem(ModelType.MENU, label, name, "", null);
    }

    public static ModelItem createVariable(VarType type,
                                           String label, String name, String value) {
        return new ModelItem(ModelType.VARIABLE, label, name, value, type);
    }

    public void addChild(ModelItem child) {
        children.add(child);
        child.parent = this;
    }

    public Set<ModelItem> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public String getLabel() {
        return label;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public String getName() {
        return name;
    }

    public ModelItem getParent() {
        return parent;
    }

    public VarType getType() {
        return type;
    }

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
