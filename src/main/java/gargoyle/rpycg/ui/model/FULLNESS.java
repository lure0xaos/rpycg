package gargoyle.rpycg.ui.model;

import java.util.Arrays;
import java.util.Comparator;

public enum FULLNESS {
    NORMAL(0),
    ALMOST(10),
    FULL(12);
    private final int size;

    FULLNESS(int size) {
        this.size = size;
    }

    public static FULLNESS determineFullness(int size) {
        return Arrays.stream(values()).sorted(Comparator.comparingInt(o -> -o.size))
                .filter(fullness -> size >= fullness.size).findFirst().orElse(NORMAL);
    }

    public int getSize() {
        return size;
    }
}
