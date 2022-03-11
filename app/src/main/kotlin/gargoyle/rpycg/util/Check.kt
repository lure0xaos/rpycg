package gargoyle.rpycg.util

object Check {
    private val PATTERN_ID = "^[_a-zA-Z](?:[._a-zA-Z0-9]*[_a-zA-Z0-9]+)?$".toRegex()
    private val PATTERN_TEXT = "^[^\n;]+$".toRegex()

    fun isFloat(value: String): Boolean =
        null != value.toFloatOrNull()

    fun isIdentifier(id: CharSequence): Boolean =
        PATTERN_ID.matches(id)

    fun isInteger(value: String): Boolean =
        null != value.toIntOrNull()

    fun isText(text: CharSequence): Boolean =
        PATTERN_TEXT.matches(text)

}
