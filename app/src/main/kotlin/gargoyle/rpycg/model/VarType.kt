package gargoyle.rpycg.model

enum class VarType(val keyword: String) {
    INT("int"), FLOAT("float"), STR("str");

    companion object {
        fun find(keyword: String): VarType =
            values().first { it.keyword == keyword }
    }
}
