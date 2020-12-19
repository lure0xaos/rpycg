package gargoyle.rpycg.fx;

import java.text.MessageFormat;

public final class FXReflection {
    private FXReflection() {
        throw new IllegalStateException(FXReflection.class.getName());
    }

    public static <T> T instantiate(String className) {
        return FXReflection.instantiate(classForName(className));
    }

    public static <T> T instantiate(Class<? extends T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new FXException(MessageFormat.format(FXConstants.MSG_ERROR_LOADING_COMPONENT, type), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForName(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ReflectiveOperationException e) {
            throw new FXException(MessageFormat.format(FXConstants.MSG_ERROR_LOADING_COMPONENT, className), e);
        }
    }
}
