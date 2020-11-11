package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public final class FXReflection {
    private FXReflection() {
        throw new IllegalStateException(getClass().getName());
    }

    @NotNull
    public static <T> T instantiate(@NotNull String className) {
        return FXReflection.instantiate(classForName(className));
    }

    @NotNull
    public static <T> T instantiate(@NotNull Class<? extends T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new FXException(MessageFormat.format(FXLoad.MSG_ERROR_LOADING_COMPONENT, type), e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Class<T> classForName(@NotNull String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ReflectiveOperationException e) {
            throw new FXException(MessageFormat.format(FXLoad.MSG_ERROR_LOADING_COMPONENT, className), e);
        }
    }
}
