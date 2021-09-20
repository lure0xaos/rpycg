package gargoyle.rpycg.model;

import gargoyle.rpycg.ui.model.FULLNESS;

import java.text.MessageFormat;
import java.util.*;

public final class ModelItem {
    private final Set<ModelItem> children = new LinkedHashSet<>(FULLNESS.FULL.getSize());
    private final String label;
    private final ModelType modelType;
    private final String name;
    private final VarType type;
    private final String value;
    private ModelItem parent;

    private ModelItem(final ModelType modelType, final String label,
                      final String name, final String value, final VarType type) {
        this.modelType = modelType;
        this.label = label;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public static ModelItem createMenu(final String label, final String name) {
        return new ModelItem(ModelType.MENU, label, name, "", null);
    }

    public static ModelItem createVariable(final VarType type,
                                           final String label, final String name, final String value) {
        return new ModelItem(ModelType.VARIABLE, label, name, value, type);
    }

    public void addChild(final ModelItem child) {
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || ModelItem.class != obj.getClass()) {
            return false;
        }
        final ModelItem modelItem = (ModelItem) obj;
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
