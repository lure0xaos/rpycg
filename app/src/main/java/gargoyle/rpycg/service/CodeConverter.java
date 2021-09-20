package gargoyle.rpycg.service;

import gargoyle.fx.FXContext;
import gargoyle.fx.FXUtil;
import gargoyle.rpycg.ex.CodeGenerationException;
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
import java.util.stream.Collectors;

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

    public CodeConverter(final FXContext context, final Settings settings) {
        keyConverter = new KeyConverter();
        this.context = context;
        this.settings = settings;
    }

    private static List<String> include(final Charset charset, final URL resource) {
        final List<String> lines;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), charset))) {
            lines = reader.lines().collect(Collectors.toCollection(LinkedList::new));
        } catch (final IOException e) {
            throw new CodeGenerationException(e.getLocalizedMessage());
        }
        return lines;
    }

    private static String indent(final int indent, final String line) {
        final StringBuilder result = new StringBuilder(line);
        for (int i = 0; i < indent; i++) {
            result.insert(0, "    ");
        }
        return result.toString();
    }

    public List<String> toCode(final ModelItem menu) {
        final ResourceBundle messages;
        try (final FXContext fxContext = context.toBuilder().setLocale(settings.getLocaleMenu()).createContext()) {
            messages = fxContext
                    .loadResources(CodeConverter.class)
                    .orElseThrow(() -> new CodeGenerationException(MessageFormat.format("No resources {resource}",
                            CodeConverter.class.getName())));
        }
        final String fileVariables = messages.containsKey(LC_FILE_VARIABLES) ? messages.getString(LC_FILE_VARIABLES) :
                MSG_GAME_VARIABLES;
        final List<String> buffer = new LinkedList<>();
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
            buffer.add("    # Define function to open the menu");
            buffer.add("    def enable_cheat_menu():");
            buffer.add("        renpy.call_in_new_context(\"show_cheat_menu\")");
            buffer.add(FXUtil.format("    config.keymap[\"cheat_menu_bind\"] = [\"#{cheatKey}\"]",
                    Map.of("cheatKey", keyConverter.toBinding(settings.getKeyCheat()))));
        }
        if (settings.getEnableConsole()) {
            buffer.add("    # Enable fast console");
            buffer.add(FXUtil.format("    config.keymap[\"console\"] = [\"#{consoleKey}\"]",
                    Map.of("consoleKey", keyConverter.toBinding(settings.getKeyConsole()))));
        }
        if (settings.getEnableDeveloper()) {
            buffer.add("    # Enable developer mode");
            buffer.add(FXUtil.format("    config.keymap[\"developer\"] = [\"#{developerKey}\"]",
                    Map.of("developerKey", keyConverter.toBinding(settings.getKeyDeveloper()))));
            buffer.add("    config.underlay.append(renpy.Keymap(cheat_menu_bind=enable_cheat_menu))");
        }
        if (settings.getEnableRollback()) {
            buffer.add("    # Enable rollback");
            buffer.add("    config.rollback_enabled = True");
        }
        if (settings.getEnableWrite()) {
            final String messageWritten = messages.containsKey(LC_MESSAGE_WRITTEN) ? messages.getString(LC_MESSAGE_WRITTEN) :
                    MSG_VARIABLES_WRITTEN;
            buffer.addAll(this.context.findResource(
                            this.context.resolveBaseName(CodeConverter.class, LOC_WRITE), EXT_RPY)
                    .map(resource -> FXUtil.format(include(this.context.getCharset(), resource), Map.of(
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

    private List<String> createCheatMenu(final ResourceBundle messages, final ModelItem root) {
        final List<String> buffer = new LinkedList<>();
        buffer.add("label show_cheat_menu:");
        buffer.add("    jump CheatMenu");
        buffer.add("label CheatMenu:");
        buffer.add("    menu:");
        buffer.addAll(createCheatSubmenu(1, messages, root, "CheatMenu"));
        buffer.add("        # nevermind");
        buffer.add(FXUtil.format("        \"~#{nevermind}~\":",
                Map.of("nevermind", messages.containsKey(LC_NEVERMIND) ?
                        messages.getString(LC_NEVERMIND) : MSG_NEVERMIND)));
        buffer.add("            return");
        return buffer;
    }

    private List<String> createCheatSubmenu(final int indent, final ResourceBundle messages, final ModelItem root, final String parentLabel) {
        final List<String> buffer = new LinkedList<>();
        for (final ModelItem item : root.getChildren()) {
            final ModelType modelType = item.getModelType();
            final String itemName = item.getName();
            final String itemLabel = item.getLabel();
            if (ModelType.VARIABLE == modelType) {
                final VarType itemType = item.getType();
                final String itemValue = item.getValue();
                buffer.add(indent(indent,
                        FXUtil.format("    # variable #{name}=#{type}(#{value}) #{label}",
                                Map.of("name", itemName,
                                        "type", itemType,
                                        "value", itemValue,
                                        "label", itemLabel)
                        )));
                final String itemTypeKeyword = itemType.getKeyword();
                if (!itemValue.isBlank()) {
                    buffer.add(indent(indent,
                            FXUtil.format("    \"$#{label}=#{value} \\[[#{name}]\\]\" :",
                                    Map.of("label", itemLabel,
                                            "value", itemValue,
                                            "name", itemName)
                            )));
                    if (VarType.STR == itemType) {
                        buffer.add(indent(indent,
                                FXUtil.format("        $#{name} = \"#{keyword}(\"#{value}\")\"",
                                        Map.of("name", itemName,
                                                "keyword", itemTypeKeyword,
                                                "value", itemValue)
                                )));
                    } else {
                        buffer.add(indent(indent,
                                FXUtil.format("        $#{name} = #{value}",
                                        Map.of("name", itemName, "value", itemValue)
                                )));
                    }
                } else {
                    buffer.add(indent(indent,
                            FXUtil.format("    \"#{label} \\[[#{name}]\\]\" :",
                                    Map.of("label", itemLabel, "name", itemName)
                            )));
                    final String prompt = messages.containsKey(LC_MESSAGE_PROMPT) ? messages.getString(LC_MESSAGE_PROMPT) :
                            MSG_MESSAGE_PROMPT;
                    buffer.add(indent(indent, FXUtil.format(
                            "        $#{name} = #{keyword}(renpy.input(\"#{value}\").strip() or #{name})",
                            Map.of("name", itemName,
                                    "keyword", itemTypeKeyword,
                                    "value", FXUtil.format(prompt, Map.of("label", itemLabel, "value", "[" + itemName + "]"))
                            ))));
                }
                buffer.add(indent(indent, FXUtil.format("        jump #{parent}", Map.of("parent", parentLabel))));
            }
            if (ModelType.MENU == modelType) {
                buffer.add(indent(indent, FXUtil.format("    # menu #{label}", Map.of("label", itemLabel))));
                buffer.add(indent(indent, FXUtil.format("    \"~#{label}~\":", Map.of("label", itemLabel))));
                buffer.add(indent(indent, FXUtil.format("        label #{name}:", Map.of("name", itemName))));
                buffer.add(indent(indent, "            menu:"));
                buffer.addAll(createCheatSubmenu(indent + 3, messages, item, itemName));
                buffer.add(indent(indent, "                # back"));
                buffer.add(indent(indent, FXUtil.format("                \"~#{back}~\":",
                        Map.of(LC_BACK, messages.containsKey(LC_BACK) ?
                                messages.getString(LC_BACK) : MSG_BACK))));
                buffer.add(indent(indent, FXUtil.format("                    jump #{parent}",
                        Map.of("parent", parentLabel))));
            }
        }
        return buffer;
    }
}
