package gargoyle.rpycg.fx;

import java.security.AccessController;
import java.security.PrivilegedAction;

public final class FXContextFactory {
    private static final FXContextBuilder builder = new FXContextBuilder();
    private static final FXHolder<FXContext> contextHolder = new FXHolder<>(builder()::createContext);

    private static final String FACTORY = "java.util.prefs.PreferencesFactory";

    static {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            System.setProperty(FACTORY, FXFilePreferencesFactory.class.getName());
            return null;
        });
    }

    private FXContextFactory() {
        throw new IllegalStateException(FXContextFactory.class.getName());
    }

    public static void changeContext(final FXContext context) {
        contextHolder.set(context);
    }

    public static FXContext currentContext() {
        return contextHolder.get();
    }

    static FXContextBuilder builder() {
        return builder;
    }
}
