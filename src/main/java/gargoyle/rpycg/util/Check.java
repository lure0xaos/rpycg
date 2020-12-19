package gargoyle.rpycg.util;

import java.util.regex.Pattern;

public final class Check {
    private static final Pattern PATTERN_ID = Pattern.compile("^[_a-zA-Z](?:[._a-zA-Z0-9]*[_a-zA-Z0-9]+)?$");
    private static final Pattern PATTERN_TEXT = Pattern.compile("^[^\n;]+$");

    private Check() {
        throw new IllegalStateException(Check.class.getName());
    }

    public static boolean isFloat(String val) {
        try {
            Float.parseFloat(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isIdentifier(CharSequence id) {
        return PATTERN_ID.matcher(id).matches();
    }

    public static boolean isInteger(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isText(CharSequence text) {
        return PATTERN_TEXT.matcher(text).matches();
    }
}
