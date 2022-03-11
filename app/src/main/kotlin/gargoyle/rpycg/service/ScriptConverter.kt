package gargoyle.rpycg.service

import gargoyle.fx.FxContext
import gargoyle.rpycg.ex.AppUserException
import gargoyle.rpycg.ex.MalformedScriptException
import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.model.VarType
import gargoyle.rpycg.util.Check
import java.text.MessageFormat
import java.util.LinkedList
import java.util.ResourceBundle

@Suppress("unused")
class ScriptConverter {
    private val resources: ResourceBundle = FxContext.current.loadResources(ScriptConverter::class)
        ?: throw AppUserException("No resources {ScriptConverter}")

    fun fromScript(lines: List<String>): ModelItem {
        val root = ModelItem.createMenu("", "")
        var menu: ModelItem? = root
        for (raw in lines) {
            if (raw.isBlank()) continue
            val line = raw.trim()
            if ('<' == line[0]) {
                val substring = line.substring(1)
                val posName = substring.lastIndexOf(';')
                val label: String
                val name: String
                if (0 < posName) {
                    label = substring.substring(0, posName).trim()
                    name = substring.substring(posName + 1).trim()
                } else {
                    label = substring
                    name = substring
                }
                val child = ModelItem.createMenu(label, name)
                menu!!.addChild(child)
                menu = child
                continue
            }
            if ('>' == line[0]) {
                menu = menu!!.parent
                if (null == menu) {
                    return root
                }
                continue
            }
            var expr = line
            val posLabel = expr.lastIndexOf(';')
            val label: String
            if (0 < posLabel) {
                label = expr.substring(posLabel + 1).trim()
                expr = expr.substring(0, posLabel).trim()
            } else {
                label = ""
            }
            var type: VarType? = null
            if (expr.isNotEmpty() && ')' == expr[expr.length - 1]) {
                val open = expr.lastIndexOf('(')
                if (0 < open) {
                    val typeValue = expr.substring(open + 1, expr.length - 1).trim()
                    type = try {
                        VarType.valueOf(typeValue.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw MalformedScriptException(
                            MessageFormat.format(
                                resources.getString(LC_ERROR_WRONG_TYPE),
                                typeValue,
                                VarType.values().contentToString()
                            ), e
                        )
                    }
                    expr = expr.substring(0, open).trim()
                }
            }
            val name: String
            val value: String
            val posEq = expr.lastIndexOf('=')
            if (0 < posEq) {
                val value0 = expr.substring(posEq + 1).trim()
                name = expr.substring(0, posEq).trim()
                val len = value0.length
                val first = value0[0]
                val last = value0[len - 1]
                if ('\'' == first && '\'' == last || '\"' == first && '\"' == last) {
                    type = VarType.STR
                    value = value0.substring(1, len - 2).trim()
                } else {
                    value = value0
                }
            } else {
                name = expr
                value = ""
            }
            if (null == type) {
                type = when {
                    value.isBlank() -> VarType.STR
                    Check.isFloat(value) -> VarType.FLOAT
                    Check.isInteger(value) -> VarType.INT
                    else -> throw MalformedScriptException(resources.getString(LC_ERROR_FAIL_TYPE))
                }
            }
            if (value.isNotBlank()) {
                when (type) {
                    VarType.INT -> if (!Check.isInteger(value)) {
                        throw MalformedScriptException(resources.getString(LC_ERROR_VALUE_TYPE))
                    }
                    VarType.FLOAT -> if (!Check.isFloat(value)) {
                        throw MalformedScriptException(resources.getString(LC_ERROR_VALUE_TYPE))
                    }
                    VarType.STR -> {}
                }
            }
            menu!!.addChild(ModelItem.createVariable(type, label, name, value))
        }
        return root
    }

    fun toScript(item: ModelItem): List<String> =
        when (item.modelType) {
            ModelType.MENU -> {
                val lines: MutableList<String> = LinkedList()
                if (item.name.isNotBlank()) lines += "<${item.label};${item.name}"
                item.getChildren().forEach { child -> lines += toScript(child) }
                if (item.name.isNotBlank()) lines += ">"
                lines
            }
            ModelType.VARIABLE -> {
                listOf("${item.name}${if (item.value.isBlank()) "" else "=${item.value}"}(${item.type.keyword})${if (item.label.isBlank() || item.label == item.name) "" else ";${item.label}"}")
            }
        }

    companion object {
        private const val LC_ERROR_FAIL_TYPE = "error.fail-type"
        private const val LC_ERROR_VALUE_TYPE = "error.value-type"
        private const val LC_ERROR_WRONG_MODEL_TYPE = "error.wrong-model-type"
        private const val LC_ERROR_WRONG_TYPE = "error.wrong-type"
    }
}
