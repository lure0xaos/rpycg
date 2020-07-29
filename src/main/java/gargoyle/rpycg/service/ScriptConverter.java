package gargoyle.rpycg.service;

import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ScriptConverter {
    private static final Pattern RE_FLOAT = Pattern.compile("^[0-9]+.[0-9]+$");
    private static final Pattern RE_INT = Pattern.compile("^[0-9]+$");

    @NotNull
    public ModelItem fromScript(@NotNull Iterable<String> lines) {
        ModelItem root = ModelItem.createMenu("");
        ModelItem menu = root;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            if (line.charAt(0) == '<') {
                ModelItem child = ModelItem.createMenu(line.substring(1).trim());
                menu.addChild(child);
                menu = child;
                continue;
            }
            if (">".equals(line.trim())) {
                menu = menu.getParent();
                if (menu == null) {
                    return root;
                }
                continue;
            }
            String expr = line.trim();
            int posLabel = expr.lastIndexOf(';');
            String label;
            if (posLabel > 0) {
                label = expr.substring(posLabel + 1).trim();
                expr = expr.substring(0, posLabel).trim();
            } else {
                label = "";
            }
            VarType type = null;
            if (!expr.isEmpty() && expr.charAt(expr.length() - 1) == ')') {
                int open = expr.lastIndexOf('(');
                if (open > 0) {
                    String typeValue = expr.substring(open + 1, expr.length() - 1).trim();
                    try {
                        type = VarType.valueOf(typeValue.toUpperCase(Locale.ENGLISH));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(String.format("wrong type %s, should be on of %s", typeValue,
                                Arrays.toString(VarType.values())), e);
                    }
                    expr = expr.substring(0, open).trim();
                }
            }
            String name;
            String value;
            int posEq = expr.lastIndexOf('=');
            if (posEq > 0) {
                String val = expr.substring(posEq + 1).trim();
                name = expr.substring(0, posEq).trim();
                int len = val.length();
                if (val.charAt(0) == '\'' && val.charAt(len - 1) == '\'' ||
                    val.charAt(0) == '\"' && val.charAt(len - 1) == '\"') {
                    type = VarType.STR;
                    value = val.substring(1, len - 2).trim();
                } else {
                    value = val;
                }
            } else {
                name = expr;
                value = "";
            }
            if (type == null) {
                if (value.isBlank()) {
                    type = VarType.STR;
                } else if (RE_FLOAT.matcher(value).matches()) {
                    type = VarType.FLOAT;
                } else if (RE_INT.matcher(value).matches()) {
                    type = VarType.INT;
                } else {
                    throw new IllegalStateException("unable determine type");
                }
            }
            menu.addChild(ModelItem.createVariable(type, label, name, value));
        }
        return root;
    }

    @NotNull
    public List<String> toScript(@NotNull ModelItem item) {
        String label = item.getLabel();
        ModelType modelType = item.getModelType();
        switch (modelType) {
            case MENU:
                List<String> lines = new LinkedList<>();
                if (!label.isBlank()) {
                    lines.add(String.format("<%s", label));
                }
                for (ModelItem child : item.getChildren()) {
                    lines.addAll(toScript(child));
                }
                if (!label.isBlank()) {
                    lines.add(">");
                }
                return lines;
            case VARIABLE:
                String name = item.getName();
                String value = item.getValue();
                String keyword = item.getType() == null ? "" : item.getType().getKeyword();
                return Collections.singletonList(String.format("%s%s(%s)%s",
                        name,
                        value.isBlank() ? "" : '=' + value,
                        keyword,
                        label.isBlank() || label.equals(name) ? "" : ';' + label
                ));
            default:
                throw new IllegalStateException("unsupported model type " + modelType);
        }
    }
}
