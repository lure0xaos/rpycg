package gargoyle.rpycg.model;

public enum VarType {
    INT("int"),
    FLOAT("float"),
    STR("str");
    private final String keyword;

    VarType(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
