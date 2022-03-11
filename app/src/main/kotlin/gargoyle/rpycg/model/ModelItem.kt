package gargoyle.rpycg.model

import gargoyle.rpycg.ui.model.FULLNESS

class ModelItem private constructor(
    val modelType: ModelType,
    val label: String,
    val name: String,
    val value: String,
    val type: VarType
) {

    private val children: MutableSet<ModelItem> = LinkedHashSet(FULLNESS.FULL.size)
    var parent: ModelItem? = null
        private set

    fun addChild(child: ModelItem) {
        children.add(child)
        child.parent = this
    }

    fun getChildren(): Set<ModelItem> = children

    override fun hashCode(): Int = arrayOf(modelType, name).contentHashCode()

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            null == other || ModelItem::class.java != other.javaClass -> false
            else -> modelType == (other as ModelItem).modelType && name == other.name
        }

    override fun toString(): String =
        "ModelItem[${modelType}]{type=${type}, label=${label}, name=${name}, value=${value},parent=${parent?.label}}"

    companion object {
        fun createMenu(label: String, name: String): ModelItem =
            ModelItem(ModelType.MENU, label, name, "", VarType.STR)

        fun createVariable(type: VarType, label: String, name: String, value: String): ModelItem =
            ModelItem(ModelType.VARIABLE, label, name, value, type)
    }

}
