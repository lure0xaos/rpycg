package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ex.MalformedScriptException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.util.Check;

import java.text.MessageFormat;
import java.util.*;

public final class ScriptConverter {
    private static final String LC_ERROR_FAIL_TYPE = "error.fail-type";
    private static final String LC_ERROR_VALUE_TYPE = "error.value-type";
    private static final String LC_ERROR_WRONG_MODEL_TYPE = "error.wrong-model-type";
    private static final String LC_ERROR_WRONG_TYPE = "error.wrong-type";
    private final ResourceBundle resources;

    public ScriptConverter() {
        resources = FXContextFactory.currentContext().loadResources(ScriptConverter.class)
                .orElseThrow(() ->
                        new AppUserException(AppUserException.LC_ERROR_NO_RESOURCES, ScriptConverter.class.getName()));
    }

    public ModelItem fromScript(final Iterable<String> lines) {
        final ModelItem root = ModelItem.createMenu("", "");
        ModelItem menu = root;
        for (final String raw : lines) {
            if (raw.isBlank()) {
                continue;
            }
            final String line = raw.trim();
            if ('<' == line.charAt(0)) {
                final String substring = line.substring(1);
                final int posName = substring.lastIndexOf(';');
                final String label;
                final String name;
                if (0 < posName) {
                    label = substring.substring(0, posName).trim();
                    name = substring.substring(posName + 1).trim();
                } else {
                    label = substring;
                    name = substring;
                }
                final ModelItem child = ModelItem.createMenu(label, name);
                menu.addChild(child);
                menu = child;
                continue;
            }
            if ('>' == line.charAt(0)) {
                menu = menu.getParent();
                if (null == menu) {
                    return root;
                }
                continue;
            }
            String expr = line;
            final int posLabel = expr.lastIndexOf(';');
            final String label;
            if (0 < posLabel) {
                label = expr.substring(posLabel + 1).trim();
                expr = expr.substring(0, posLabel).trim();
            } else {
                label = "";
            }
            VarType type = null;
            if (!expr.isEmpty() && ')' == expr.charAt(expr.length() - 1)) {
                final int open = expr.lastIndexOf('(');
                if (0 < open) {
                    final String typeValue = expr.substring(open + 1, expr.length() - 1).trim();
                    try {
                        type = VarType.valueOf(typeValue.toUpperCase(Locale.ENGLISH));
                    } catch (final IllegalArgumentException e) {
                        throw new MalformedScriptException(
                                MessageFormat.format(resources.getString(LC_ERROR_WRONG_TYPE),
                                        typeValue, Arrays.toString(VarType.values())), e);
                    }
                    expr = expr.substring(0, open).trim();
                }
            }
            final String name;
            final String value;
            final int posEq = expr.lastIndexOf('=');
            if (0 < posEq) {
                final String val = expr.substring(posEq + 1).trim();
                name = expr.substring(0, posEq).trim();
                final int len = val.length();
                final char first = val.charAt(0);
                final char last = val.charAt(len - 1);
                if ('\'' == first && '\'' == last || '\"' == first && '\"' == last) {
                    type = VarType.STR;
                    value = val.substring(1, len - 2).trim();
                } else {
                    value = val;
                }
            } else {
                name = expr;
                value = "";
            }
            if (null == type) {
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

    public List<String> toScript(final ModelItem item) {
        final String label = item.getLabel();
        final String name = item.getName();
        final ModelType modelType = item.getModelType();
        switch (modelType) {
            case MENU:
                final List<String> lines = new LinkedList<>();
                if (!name.isBlank()) {
                    lines.add(MessageFormat.format("<{0};{1}", label, name));
                }
                for (final ModelItem child : item.getChildren()) {
                    lines.addAll(toScript(child));
                }
                if (!name.isBlank()) {
                    lines.add(">");
                }
                return lines;
            case VARIABLE:
                final String value = item.getValue();
                final String keyword = null == item.getType() ? "" : item.getType().getKeyword();
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
