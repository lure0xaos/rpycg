package gargoyle.rpycg.ui.model


enum class FULLNESS(val size: Int) {
    NORMAL(0), ALMOST(10), FULL(12);

    companion object {
        fun determineFullness(size: Int): FULLNESS =
            values()
                .sortedBy { -it.size }
                .firstOrNull { size >= it.size } ?: NORMAL
    }
}
