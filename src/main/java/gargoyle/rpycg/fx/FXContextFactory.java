package gargoyle.rpycg.fx;

import gargoyle.rpycg.util.Check;
import javafx.application.Application;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FXContextFactory {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXContextFactory")
    public static final String LC_ERROR_NO_HOST_SERVICES = "error.no-host-services";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXContextFactory")
    public static final String LC_ERROR_NO_PARAMETERS = "error.no-parameters";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXContextFactory")
    public static final String LC_ERROR_NO_PREFERENCES = "error.no-preferences";
    private static final FXHolder<FXContextImpl> context = new FXHolder<>(() ->
            new FXContextImpl(StandardCharsets.UTF_8, Locale.getDefault(),
                    Thread.currentThread().getContextClassLoader()));

    private FXContextFactory() {
        throw new IllegalStateException(getClass().getName());
    }

    public static void changeLocale(Locale locale) {
        getContext().setLocale(locale);
    }

    @NotNull
    private static FXContextImpl getContext() {
        return context.get();
    }

    @NotNull
    public static FXContext currentContext() {
        return getContext();
    }

    @NotNull
    public static FXContext forLocale(@NotNull FXContext original, @NotNull Locale locale) {
        FXContextImpl fxContext = copy(original);
        fxContext.setLocale(locale);
        return fxContext;
    }

    @NotNull
    private static FXContextImpl copy(@NotNull FXContext original) {
        FXContextImpl fxContext = new FXContextImpl(original.getCharset(), original.getLocale(),
                original.getClassLoader());
        fxContext.setHostServices(Check.requireNonNull(original.getHostServices(), () ->
                FXLoad.loadResources(fxContext, FXContextFactory.class)
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_HOST_SERVICES))
                        .orElse("")));
        fxContext.setPreferences(Check.requireNonNull(original.getPreferences(), () ->
                FXLoad.loadResources(fxContext, FXContextFactory.class)
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_PREFERENCES))
                        .orElse("")));
        fxContext.setParameters(Check.requireNonNull(original.getParameters(), () ->
                FXLoad.loadResources(fxContext, FXContextFactory.class)
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_PARAMETERS))
                        .orElse("")));
        fxContext.setSkin(original.getSkin());
        return fxContext;
    }

    public static void initializeContext(@NotNull Application application,
                                         @NotNull Class<? extends FXApplication> appClass) {
        FXContextImpl fxContext = getContext();
        fxContext.setClassLoader(appClass.getClassLoader());
        fxContext.setHostServices(application.getHostServices());
        fxContext.setParameters(application.getParameters());
        fxContext.initializePreferences(appClass);
        fxContext.initializeSkin(Check.requireNonNull(fxContext.getPreferences(), () ->
                FXLoad.loadResources(fxContext, FXContextFactory.class)
                        .map(resources -> resources.getString(LC_ERROR_NO_PARAMETERS))
                        .orElse("")), application.getParameters());
        fxContext.initializeLocale();
    }

    @NotNull
    public static FXContext snapshot(@NotNull FXContext original) {
        return copy(original);
    }
}
