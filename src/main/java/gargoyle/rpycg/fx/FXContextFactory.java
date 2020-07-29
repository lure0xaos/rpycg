package gargoyle.rpycg.fx;

import javafx.application.Application;
import org.jetbrains.annotations.NotNull;

public final class FXContextFactory {
    private static FXContextImpl context;

    private FXContextFactory() {
    }

    @NotNull
    public static FXContext currentContext() {
        return getContext();
    }

    @NotNull
    private static synchronized FXContextImpl getContext() {
        if (context == null) {
            context = new FXContextImpl();
            return context;
        }
        return context;
    }

    public static void initializeContext(@NotNull Application application) {
        FXContextImpl fxContext = getContext();
        fxContext.setClassLoader(application.getClass().getClassLoader());
        fxContext.setHostServices(application.getHostServices());
        fxContext.setParameters(application.getParameters());
    }
}
