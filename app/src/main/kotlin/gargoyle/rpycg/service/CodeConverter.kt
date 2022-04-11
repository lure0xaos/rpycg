package gargoyle.rpycg.service

import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil
import gargoyle.fx.FxUtil.get
import gargoyle.rpycg.ex.CodeGenerationException
import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.model.Settings
import gargoyle.rpycg.model.VarType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.util.ResourceBundle

class CodeConverter(private val context: FxContext, private val settings: Settings) {

    private val keyConverter: KeyConverter = KeyConverter()

    fun toCode(menu: ModelItem): List<String> {
        val messages: ResourceBundle = context.toBuilder().setLocale(settings.getLocaleMenu()).build().use {
            it.loadResources(CodeConverter::class) ?: error("No resources {CodeConverter}")
        }
        val fileVariables = messages[LC_FILE_VARIABLES, MSG_GAME_VARIABLES]
        val buffer: MutableList<String> = mutableListOf()
        buffer += "init 999 python:"
        if (settings.getEnableConsole()) {
            buffer += "    # Enable console"
            buffer += "    config.console = True"
            buffer += "    persistent._console_short = False"
        }
        if (settings.getEnableDeveloper()) {
            buffer += "    # Enable developer mode"
            buffer += "    config.developer = True"
        }
        if (settings.getEnableCheat()) {
            buffer += "    # Define function to open the menu"
            buffer += "    def enable_cheat_menu():"
            buffer += "        renpy.call_in_new_context(\"show_cheat_menu\")"
            buffer += "    config.keymap[\"cheat_menu_bind\"] = [\"${keyConverter.toBinding(settings.getKeyCheat())}\"]"
        }
        if (settings.getEnableConsole()) {
            buffer += "    # Enable fast console"
            buffer += "    config.keymap[\"console\"] = [\"${keyConverter.toBinding(settings.getKeyConsole())}\"]"
        }
        if (settings.getEnableDeveloper()) {
            buffer += "    # Enable developer mode"
            buffer += "    config.keymap[\"developer\"] = [\"${keyConverter.toBinding(settings.getKeyDeveloper())}\"]"
            buffer += "    config.underlay.append(renpy.Keymap(cheat_menu_bind=enable_cheat_menu))"
        }
        if (settings.getEnableRollback()) {
            buffer += "    # Enable rollback"
            buffer += "    config.rollback_enabled = True"
        }
        if (settings.getEnableWrite()) {
            val messageWritten = messages[LC_MESSAGE_WRITTEN, MSG_VARIABLES_WRITTEN]
            context.findResource(context.resolveBaseName(CodeConverter::class, LOC_WRITE), EXT_RPY)?.let {
                buffer += FxUtil.format(
                    include(context.charset, it), mapOf(
                        KEY_WRITE to keyConverter.toBinding(settings.getKeyWrite()),
                        KEY_MESSAGE_WRITTEN to messageWritten,
                        KEY_FILE_VARIABLES to fileVariables + EXT_TXT
                    )
                )
            }
        }
        if (settings.getEnableCheat()) {
            buffer += createCheatMenu(messages, menu)
        }
        return buffer
    }

    private fun createCheatMenu(messages: ResourceBundle, root: ModelItem): List<String> {
        val buffer: MutableList<String> = mutableListOf()
        buffer += "label show_cheat_menu:"
        buffer += "    jump CheatMenu"
        buffer += "label CheatMenu:"
        buffer += "    menu:"
        buffer += createCheatSubmenu(1, messages, root, "CheatMenu")
        buffer += "        # nevermind"
        buffer += "        \"~${messages[LC_NEVERMIND, MSG_NEVERMIND]}~\":"
        buffer += "            return"
        return buffer
    }

    private fun createCheatSubmenu(
        indent: Int,
        messages: ResourceBundle,
        root: ModelItem,
        parentLabel: String
    ): List<String> {
        val buffer: MutableList<String> = mutableListOf()
        for (item in root.getChildren()) {
            val modelType = item.modelType
            val itemName = item.name
            val itemLabel = item.label
            when {
                ModelType.VARIABLE == modelType -> {
                    val itemType = item.type
                    val itemValue = item.value
                    buffer += indent(indent, "    # variable ${itemName}=${itemType}(${itemValue}) ${itemLabel}")
                    val itemTypeKeyword = itemType.keyword
                    if (itemValue.isNotBlank()) {
                        buffer += indent(indent, "    \"$${itemLabel}=${itemValue} \\[[${itemName}]\\]\" :")
                        buffer += if (VarType.STR == itemType) {
                            indent(indent, "        $${itemName} = \"${itemTypeKeyword}(\"${itemValue}\")\"")
                        } else {
                            indent(indent, "        $${itemName} = ${itemValue}")
                        }
                    } else {
                        buffer += indent(indent, "    \"${itemLabel} \\[[${itemName}]\\]\" :")
                        val prompt = messages[LC_MESSAGE_PROMPT, MSG_MESSAGE_PROMPT]
                        buffer += indent(
                            indent,
                            "        $${itemName} = ${itemTypeKeyword}(renpy.input(\"${
                                FxUtil.format(prompt, mapOf("label" to itemLabel, "value" to "[$itemName]"))
                            }\").strip() or ${itemName})",
                        )
                    }
                    buffer += indent(indent, "        jump ${parentLabel}")
                }
                ModelType.MENU == modelType -> {
                    buffer += indent(indent, "    # menu ${itemLabel}")
                    buffer += indent(indent, "    \"~${itemLabel}~\":")
                    buffer += indent(indent, "        label ${itemName}:")
                    buffer += indent(indent, "            menu:")
                    buffer += createCheatSubmenu(indent + 3, messages, item, itemName)
                    buffer += indent(indent, "                # back")
                    buffer += indent(
                        indent,
                        "                \"~${messages[LC_BACK, MSG_BACK]}~\":"
                    )
                    buffer += indent(indent, "                    jump ${parentLabel}")
                }
            }
        }
        return buffer
    }

    companion object {
        private const val EXT_RPY = "rpy"
        private const val EXT_TXT = ".txt"
        private const val KEY_FILE_VARIABLES = "fileVariables"
        private const val KEY_MESSAGE_WRITTEN = "messageWritten"
        private const val KEY_WRITE = "keyWrite"
        private const val LC_BACK = "back"
        private const val LC_FILE_VARIABLES = "file-variables"
        private const val LC_MESSAGE_PROMPT = "message-prompt"
        private const val LC_MESSAGE_WRITTEN = "message-written"
        private const val LC_NEVERMIND = "nevermind"
        private const val LOC_WRITE = "write_variables_to_file"
        private const val MSG_BACK = "Back"
        private const val MSG_GAME_VARIABLES = "Game Variables"
        private const val MSG_MESSAGE_PROMPT = "Change {0} from {1} to"
        private const val MSG_NEVERMIND = "Nevermind"
        private const val MSG_VARIABLES_WRITTEN = "Game variables written to file."
        private fun include(charset: Charset, resource: URL): List<String> =
            try {
                BufferedReader(InputStreamReader(resource.openStream(), charset)).use { return it.readLines() }
            } catch (e: IOException) {
                throw CodeGenerationException(e.localizedMessage)
            }

        private fun indent(indent: Int, line: String): String {
            val result = StringBuilder(line)
            repeat(indent) { result.insert(0, "    ") }
            return result.toString()
        }
    }

}
