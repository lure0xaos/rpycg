package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ex.MalformedScriptException;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.util.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ScriptConverter {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.ScriptConverter")
    private static final String LC_ERROR_FAIL_TYPE = "error.fail-type";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.ScriptConverter")
    private static final String LC_ERROR_VALUE_TYPE = "error.value-type";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.ScriptConverter")
    private static final String LC_ERROR_WRONG_MODEL_TYPE = "error.wrong-model-type";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.ScriptConverter")
    private static final String LC_ERROR_WRONG_TYPE = "error.wrong-type";

    private final ResourceBundle resources;

    public ScriptConverter() {
        resources = FXLoad.loadResources(getClass())
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_RESOURCES, getClass().getName()));
    }

    @NotNull
    public ModelItem fromScript(@NotNull Iterable<String> lines) {
        ModelItem root = ModelItem.createMenu("", "");
        ModelItem menu = root;
        for (String raw : lines) {
            if (raw.isBlank()) {
                continue;
            }
            String line = raw.trim();
            if (line.charAt(0) == '<') {
                String substring = line.substring(1);
                int posName = substring.lastIndexOf(';');
                String label;
                String name;
                if (posName > 0) {
                    label = substring.substring(0, posName).trim();
                    name = substring.substring(posName + 1).trim();
                } else {
                    label = substring;
                    name = substring;
                }
                ModelItem child = ModelItem.createMenu(label, name);
                menu.addChild(child);
                menu = child;
                continue;
            }
            if (line.charAt(0) == '>') {
                menu = menu.getParent();
                if (menu == null) {
                    return root;
                }
                continue;
            }
            String expr = line;
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
                        throw new MalformedScriptException(MessageFormat.format(resources.getString(LC_ERROR_WRONG_TYPE),
                                typeValue, Arrays.toString(VarType.values())), e);
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
                char first = val.charAt(0);
                char last = val.charAt(len - 1);
                if (first == '\'' && last == '\'' || first == '\"' && last == '\"') {
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
                } else if (Check.isFloat(value)) {
                    type = VarType.FLOAT;
                } else if (Check.isInteger(value)) {
                    type = VarType.INT;
                } else {
                    throw new MalformedScriptException(resources.getString(LC_ERROR_FAIL_TYPE));
                }
            }
            if (!value.isBlank()) {
                switch (type) {
                    case INT:
                        if (!Check.isInteger(value)) {
                            throw new MalformedScriptException(resources.getString(LC_ERROR_VALUE_TYPE));
                        }
                        break;
                    case FLOAT:
                        if (!Check.isFloat(value)) {
                            throw new MalformedScriptException(resources.getString(LC_ERROR_VALUE_TYPE));
                        }
                        break;
                    case STR:
                        break;
                }
            }
            menu.addChild(ModelItem.createVariable(type, label, name, value));
        }
        return root;
    }

    @NotNull
    public List<String> toScript(@NotNull ModelItem item) {
        String label = item.getLabel();
        String name = item.getName();
        ModelType modelType = item.getModelType();
        switch (modelType) {
            case MENU:
                List<String> lines = new LinkedList<>();
                if (!name.isBlank()) {
                    lines.add(MessageFormat.format("<{0};{1}", label, name));
                }
                for (ModelItem child : item.getChildren()) {
                    lines.addAll(toScript(child));
                }
                if (!name.isBlank()) {
                    lines.add(">");
                }
                return lines;
            case VARIABLE:
                String value = item.getValue();
                String keyword = item.getType() == null ? "" : item.getType().getKeyword();
                return Collections.singletonList(MessageFormat.format("{0}{1}({2}){3}",
                        name,
                        value.isBlank() ? "" : '=' + value,
                        keyword,
                        label.isBlank() || label.equals(name) ? "" : ';' + label
                ));
            default:
                throw new IllegalStateException(resources.getString(LC_ERROR_WRONG_MODEL_TYPE) + " " + modelType);
        }
    }
}
