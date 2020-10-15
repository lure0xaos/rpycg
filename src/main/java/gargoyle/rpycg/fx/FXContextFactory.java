package gargoyle.rpycg.fx;

import gargoyle.rpycg.util.Check;
import javafx.application.Application;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FXContextFactory {
    public static final String LC_ERROR_NO_HOST_SERVICES = "error.no-host-services";
    public static final String LC_ERROR_NO_PARAMETERS = "error.no-parameters";
    public static final String LC_ERROR_NO_PREFERENCES = "error.no-preferences";
    private static FXContextImpl context;

    private FXContextFactory() {
    }

    public static void changeLocale(Locale locale) {
        FXContextImpl fxContext = getContext();
        fxContext.setLocale(locale);
    }

    @NotNull
    private static synchronized FXContextImpl getContext() {
        if (context == null) {
            context = new FXContextImpl(StandardCharsets.UTF_8, Locale.getDefault(),
                    Thread.currentThread().getContextClassLoader());
            return context;
        }
        return context;
    }

    @NotNull
    public static FXContext currentContext() {
        return getContext();
    }

    @NotNull
    public static FXContext forLocale(@NotNull FXContext original, @NotNull Locale locale) {
        FXContextImpl fxContext = new FXContextImpl(original.getCharset(), locale, original.getClassLoader());
        fxContext.setHostServices(Check.requireNonNull(original.getHostServices(), () ->
                FXLoad.loadResources(fxContext, FXLoad.getBaseName(FXContextFactory.class))
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_HOST_SERVICES))
                        .orElse("")));
        fxContext.setPreferences(Check.requireNonNull(original.getPreferences(), () ->
                FXLoad.loadResources(fxContext, FXLoad.getBaseName(FXContextFactory.class))
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_PREFERENCES))
                        .orElse("")));
        fxContext.setParameters(Check.requireNonNull(original.getParameters(), () ->
                FXLoad.loadResources(fxContext, FXLoad.getBaseName(FXContextFactory.class))
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_PARAMETERS))
                        .orElse("")));
        fxContext.setSkin(original.getSkin());
        return fxContext;
    }

    public static void initializeContext(@NotNull Application application) {
        FXContextImpl fxContext = getContext();
        Class<? extends Application> applicationClass = application.getClass();
        fxContext.setClassLoader(applicationClass.getClassLoader());
        fxContext.setHostServices(application.getHostServices());
        fxContext.setParameters(application.getParameters());
        fxContext.initializePreferences(applicationClass);
        fxContext.initializeSkin(Check.requireNonNull(fxContext.getPreferences(), () ->
                FXLoad.loadResources(fxContext, FXLoad.getBaseName(FXContextFactory.class))
                        .map(resources -> resources.getString(LC_ERROR_NO_PARAMETERS))
                        .orElse("")), application.getParameters());
        fxContext.initializeLocale();
    }
}
