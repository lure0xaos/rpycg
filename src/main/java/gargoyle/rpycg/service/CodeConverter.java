package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.CodeGenerationException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXUtil;
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

import static gargoyle.rpycg.ex.AppUserException.LC_ERROR_NO_RESOURCES;

public final class CodeConverter {
    private static final String EXT_RPY = "rpy";
    private static final String EXT_TXT = ".txt";
    private static final String KEY_FILE_VARIABLES = "fileVariables";
    private static final String KEY_MESSAGE_WRITTEN = "messageWritten";
    private static final String KEY_WRITE = "keyWrite";
    private static final String LC_BACK = "back";
    private static final String LC_FILE_VARIABLES = "file-variables";
    private static final String LC_MESSAGE_PROMPT = "message-prompt";
    private static final String LC_MESSAGE_WRITTEN = "message-written";
    private static final String LC_NEVERMIND = "nevermind";
    private static final String LOC_WRITE = "write_variables_to_file";
    private static final String MSG_BACK = "Back";
    private static final String MSG_GAME_VARIABLES = "Game Variables";
    private static final String MSG_MESSAGE_PROMPT = "Change {0} from {1} to";
    private static final String MSG_NEVERMIND = "Nevermind";
    private static final String MSG_VARIABLES_WRITTEN = "Game variables written to file.";
    private final FXContext context;
    private final KeyConverter keyConverter;
    private final Settings settings;

    public CodeConverter(FXContext context, Settings settings) {
        keyConverter = new KeyConverter();
        this.context = context;
        this.settings = settings;
    }

    private static List<String> include(Charset charset, URL resource) {
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

    private static String indent(int indent, String line) {
        StringBuilder result = new StringBuilder(line);
        for (int i = 0; i < indent; i++) {
            result.insert(0, "    ");
        }
        return result.toString();
    }

    private List<String> createCheatMenu(ResourceBundle messages, ModelItem root) {
        List<String> buffer = new LinkedList<>();
        buffer.add("label show_cheat_menu:");
        buffer.add("    jump " + "CheatMenu");
        buffer.add("label " + "CheatMenu" + ":");
        buffer.add("    menu:");
        buffer.addAll(createCheatSubmenu(1, messages, root, "CheatMenu"));
        buffer.add("        # nevermind");
        buffer.add("        \"~" + (messages.containsKey(LC_NEVERMIND) ?
                messages.getString(LC_NEVERMIND) : MSG_NEVERMIND) + "~\":");
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
                    String prompt = messages.containsKey(LC_MESSAGE_PROMPT) ? messages.getString(LC_MESSAGE_PROMPT) :
                            MSG_MESSAGE_PROMPT;
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
                buffer.add(indent(indent, "                \"~" + (messages.containsKey(LC_BACK) ?
                        messages.getString(LC_BACK) : MSG_BACK) + "~\":"));
                buffer.add(indent(indent, "                    jump " + parentLabel));
            }
        }
        return buffer;
    }

    public List<String> toCode(ModelItem menu) {
        ResourceBundle messages = FXContextFactory.forLocale(context, settings.getLocaleMenu())
                .loadResources(CodeConverter.class)
                .orElseThrow(() -> new CodeGenerationException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                        CodeConverter.class.getName())));
        String fileVariables = messages.containsKey(LC_FILE_VARIABLES) ? messages.getString(LC_FILE_VARIABLES) :
                MSG_GAME_VARIABLES;
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
            String messageWritten = messages.containsKey(LC_MESSAGE_WRITTEN) ? messages.getString(LC_MESSAGE_WRITTEN) :
                    MSG_VARIABLES_WRITTEN;
            buffer.addAll(context.findResource(
                    context.getBaseName(CodeConverter.class, LOC_WRITE), EXT_RPY)
                    .map(resource -> FXUtil.format(include(context.getCharset(), resource), Map.of(
                            KEY_WRITE, keyConverter.toBinding(settings.getKeyWrite()),
                            KEY_MESSAGE_WRITTEN, messageWritten,
                            KEY_FILE_VARIABLES, fileVariables + EXT_TXT
                    ))).orElse(List.of()));
        }
        if (settings.getEnableCheat()) {
            buffer.addAll(createCheatMenu(messages, menu));
        }
        return buffer;
    }
}
