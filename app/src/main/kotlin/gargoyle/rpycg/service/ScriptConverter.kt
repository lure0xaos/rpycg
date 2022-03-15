package gargoyle.rpycg.service

import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil.get
import gargoyle.rpycg.ex.MalformedScriptException
import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.model.VarType
import java.text.MessageFormat
import java.util.ResourceBundle

class ScriptConverter {
    private val resources: ResourceBundle =
        FxContext.current.loadResources(ScriptConverter::class) ?: error("No resources {ScriptConverter}")

    fun fromScript(lines: List<String>): ModelItem {
        val root = ModelItem.createMenu("", "")
        var menu: ModelItem = root
        for (raw in lines) {
            if (raw.isBlank()) continue
            val line = raw.trim()
            if (line.startsWith('<')) {
                ModelItem.createMenu(
                    line.trimStart('<').substringBefore(';').trim(),
                    line.trimStart('<').substringAfter(';').trim()
                ).also {
                    menu.addChild(it)
                    menu = it
                }
                continue
            }
            if (line.startsWith('>')) {
                if (menu.parent == null) return root
                menu = menu.parent!!
                continue
            }
            val expr0: String = if (line.contains(';')) line.substringBefore(';').trim() else line
            val label: String = if (line.contains(';')) line.substringAfter(';').trim() else ""
            var type: VarType? = null
            val expr1: String
            if (expr0.endsWith(')') && expr0.contains('(')) {
                expr1 = expr0.substringBefore('(').trim()
                type = try {
                    VarType.find(expr0.substringAfter('(').substringBefore(')').trim())
                } catch (e: NoSuchElementException) {
                    throw MalformedScriptException(
                        MessageFormat.format(
                            resources[LC_ERROR_WRONG_TYPE],
                            e.message,
                            VarType.values().contentToString()
                        ), e
                    )
                }
            } else {
                expr1 = expr0
            }
            val name: String
            val value: String
            if (expr1.contains('=')) {
                val value0 = expr1.substringAfterLast('=').trim()
                name = expr1.substringBeforeLast('=').trim()
                if ((value0.startsWith('\'') && value0.endsWith('\'')) ||
                    (value0.startsWith('\"') && value0.endsWith('\"'))
                ) {
                    type = VarType.STR
                    value = value0.trim('\'', '\"').trim()
                } else {
                    value = value0
                }
            } else {
                name = expr1
                value = ""
            }
            if (null == type) type = when {
                value.isBlank() -> VarType.STR
                null != value.toFloatOrNull() -> VarType.FLOAT
                null != value.toIntOrNull() -> VarType.INT
                else -> throw MalformedScriptException(resources[LC_ERROR_FAIL_TYPE])
            }
            if (value.isNotBlank())
                when (type) {
                    VarType.INT -> if (null == value.toIntOrNull())
                        throw MalformedScriptException(resources[LC_ERROR_VALUE_TYPE])
                    VarType.FLOAT -> if (null == value.toFloatOrNull())
                        throw MalformedScriptException(resources[LC_ERROR_VALUE_TYPE])
                    VarType.STR -> {}
                }
            menu.addChild(ModelItem.createVariable(type, label, name, value))
        }
        return root
    }

    fun toScript(item: ModelItem): List<String> =
        when (item.modelType) {
            ModelType.MENU ->
                if (item.name.isNotBlank()) listOf("<${item.label};${item.name}") +
                        item.getChildren().flatMap { toScript(it) } + listOf(">")
                else item.getChildren().flatMap { toScript(it) }
            ModelType.VARIABLE ->
                listOf("${item.name}${if (item.value.isBlank()) "" else "=${item.value}"}(${item.type.keyword})${if (item.label.isBlank() || item.label == item.name) "" else ";${item.label}"}")
        }

    companion object {
        private const val LC_ERROR_FAIL_TYPE = "error.fail-type"
        private const val LC_ERROR_VALUE_TYPE = "error.value-type"
        private const val LC_ERROR_WRONG_MODEL_TYPE = "error.wrong-model-type"
        private const val LC_ERROR_WRONG_TYPE = "error.wrong-type"
    }
}
