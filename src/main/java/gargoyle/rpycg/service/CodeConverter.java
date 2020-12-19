package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.CodeGenerationException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.Settings;
import gargoyle.rpycg.model.VarType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gargoyle.rpycg.ex.AppUserException.LC_ERROR_NO_RESOURCES;

public final class CodeConverter {
    public static final String GAME_VARIABLES = "Game Variables.txt";
    public static final int SPACES = 4;
    private final FXContext context;
    private final String fileVariables;
    private final KeyConverter keyConverter;
    private final Settings settings;
    private final int spaces;

    public CodeConverter(FXContext context, Settings settings, int spaces, String fileVariables) {
        keyConverter = new KeyConverter();
        this.context = context;
        this.settings = settings;
        this.spaces = spaces;
        this.fileVariables = fileVariables;
    }

    private static List<String> format(List<String> format, Map<String, Object> values) {
        return format.stream().map(s -> format(s, values)).collect(Collectors.toList());
    }

    private static String format(String format, Map<String, Object> values) {
        return Pattern.compile("\\$\\{(\\w+)}").matcher(format).replaceAll(match -> {
            Object value = values.get(match.group(1));
            return value != null ? value.toString() : match.group(0);
        });
    }

    private List<String> createCheatMenu(ResourceBundle messages, ModelItem root) {
        List<String> buffer = new LinkedList<>();
        buffer.add("label show_cheat_menu:");
        buffer.add("    jump CheatMenu");
        buffer.add("label CheatMenu:");
        buffer.add("    menu:");
        buffer.addAll(createCheatSubmenu(1, messages, root, "CheatMenu"));
        buffer.add("        # nevermind");
        buffer.add("        \"~" + messages.getString("nevermind") + "~\":");
        buffer.add("            return");
        return buffer;
    }

    private List<String> createCheatSubmenu(int indent, ResourceBundle messages, ModelItem root, String parentLabel) {
        List<String> buffer = new LinkedList<>();
        for (ModelItem item : root.getChildren()) {
            ModelType modelType = item.getModelType();
            String itemName = item.getName();
            String itemLabel = item.getLabel();
            if (modelType == ModelType.VARIABLE) {
                VarType itemType = item.getType();
                String itemValue = item.getValue();
                buffer.add(indent(indent,
                        "    # variable " + itemName + "=" + itemType + "(" + itemValue + ") " + itemLabel));
                String itemTypeKeyword = itemType.getKeyword();
                if (!itemValue.isBlank()) {
                    buffer.add(indent(indent,
                            "    \"" + itemLabel + "=" + itemValue + " \\[[" + itemName + "]\\]\" :"));
                    if (itemType == VarType.STR) {
                        buffer.add(indent(indent,
                                "        $" + itemName + " = \"" + itemTypeKeyword + "(\"" + itemValue + "\")\""));
                    } else {
                        buffer.add(indent(indent,
                                "        $" + itemName + " = " + itemValue));
                    }
                } else {
                    buffer.add(indent(indent,
                            "    \"" + itemLabel + " \\[[" + itemName + "]\\]\" :"));
                    String prompt = messages.getString("message-prompt");
                    buffer.add(indent(indent,
                            "        $" + itemName + " = " + itemTypeKeyword + "(renpy.input(\"" +
                                    MessageFormat.format(prompt, itemLabel, "[" + itemName + "]")
                                    + "\").strip() or " + itemName + ")"));
                }
                buffer.add(indent(indent, "        jump " + parentLabel));
            }
            if (modelType == ModelType.MENU) {
                buffer.add(indent(indent, "    # menu " + itemLabel));
                buffer.add(indent(indent, "    \"~" + itemLabel + "~\":"));
                buffer.add(indent(indent, "        label " + itemName + ":"));
                buffer.add(indent(indent, "            menu:"));
                buffer.addAll(createCheatSubmenu(indent + 3, messages, item, itemName));
                buffer.add(indent(indent, "                # back"));
                buffer.add(indent(indent, "                \"~" + messages.getString("back") + "~\":"));
                buffer.add(indent(indent, "                    jump " + parentLabel));
            }
        }
        return buffer;
    }

    private List<String> include(Charset charset, URL resource) {
        List<String> lines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), charset))) {
            String line;
            while (null != (line = reader.readLine())) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new CodeGenerationException(e.getLocalizedMessage());
        }
        return lines;
    }

    private String indent(int indent, String line) {
        StringBuilder result = new StringBuilder(line);
        for (int i = 0; i < indent; i++) {
            for (int s = 0; s < spaces; s++) {
                result.insert(0, " ");
            }
        }
        return result.toString();
    }

    public List<String> toCode(ModelItem menu) {
        ResourceBundle messages = FXContextFactory.forLocale(context, settings.getLocaleMenu())
                .loadResources(CodeConverter.class)
                .orElseThrow(() -> new CodeGenerationException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                        CodeConverter.class.getName())));
        List<String> buffer = new LinkedList<>();
        buffer.add("init 999 python:");
        if (settings.getEnableConsole()) {
            buffer.add("    # Enable console");
            buffer.add("    config.console = True");
            buffer.add("    persistent._console_short = False");
        }
        if (settings.getEnableDeveloper()) {
            buffer.add("    # Enable developer mode");
            buffer.add("    config.developer = True");
        }
        if (settings.getEnableCheat()) {
            String cheatKey = keyConverter.toBinding(settings.getKeyCheat());
            buffer.add("    # Define function to open the menu");
            buffer.add("    def enable_cheat_menu():");
            buffer.add("        renpy.call_in_new_context(\"show_cheat_menu\")");
            buffer.add("    config.keymap[\"cheat_menu_bind\"] = [\"" + cheatKey + "\"]");
        }
        if (settings.getEnableConsole()) {
            String consoleKey = keyConverter.toBinding(settings.getKeyConsole());
            buffer.add("    # Enable fast console");
            buffer.add("    config.keymap[\"console\"] = [\"" + consoleKey + "\"]");
        }
        if (settings.getEnableDeveloper()) {
            String developerKey = keyConverter.toBinding(settings.getKeyDeveloper());
            buffer.add("    # Enable developer mode");
            buffer.add("    config.keymap[\"developer\"] = [\"" + developerKey + "\"]");
            buffer.add("    config.underlay.append(renpy.Keymap(cheat_menu_bind=enable_cheat_menu))");
        }
        if (settings.getEnableRollback()) {
            buffer.add("    # Enable rollback");
            buffer.add("    config.rollback_enabled = True");
        }
        if (settings.getEnableWrite()) {
            String messageWritten = messages.getString("message-written");
            buffer.addAll(context.findResource(
                    context.getBaseName(CodeConverter.class, "write_variables_to_file"), "rpy")
                    .map(resource -> format(include(context.getCharset(), resource), Map.of(
                            "keyWrite", keyConverter.toBinding(settings.getKeyWrite()),
                            "messageWritten", messageWritten,
                            "fileVariables", fileVariables
                    ))).orElse(List.of()));
        }
        if (settings.getEnableCheat()) {
            buffer.addAll(createCheatMenu(messages, menu));
        }
        return buffer;
    }
}
