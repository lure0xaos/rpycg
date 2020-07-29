package gargoyle.rpycg.model;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum VarType {
    INT,
    FLOAT,
    STR;

    @NotNull
    public String getKeyword() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
