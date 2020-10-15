package gargoyle.rpycg.model;

import org.jetbrains.annotations.NotNull;

public enum VarType {
    INT("int"),
    FLOAT("float"),
    STR("str");

    @NotNull
    private final String keyword;

    VarType(@NotNull String keyword) {
        this.keyword = keyword;
    }

    @NotNull
    public String getKeyword() {
        return keyword;
    }
}
