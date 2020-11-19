package gargoyle.rpycg.fx;

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
        throw new IllegalStateException(FXContextFactory.class.getName());
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
        fxContext.setHostServices(FXUtil.requireNonNull(original.getHostServices(), () ->
                fxContext.loadResources(FXContextFactory.class)
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_HOST_SERVICES))
                        .orElse("")));
        fxContext.setPreferences(FXUtil.requireNonNull(original.getPreferences(), () ->
                fxContext.loadResources(FXContextFactory.class)
                        .map(resourceBundle -> resourceBundle.getString(LC_ERROR_NO_PREFERENCES))
                        .orElse("")));
        fxContext.setParameters(FXUtil.requireNonNull(original.getParameters(), () ->
                fxContext.loadResources(FXContextFactory.class)
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
        fxContext.initializeSkin(FXUtil.requireNonNull(fxContext.getPreferences(), () ->
                fxContext.loadResources(FXContextFactory.class)
                        .map(resources -> resources.getString(LC_ERROR_NO_PARAMETERS))
                        .orElse("")), application.getParameters());
        fxContext.initializeLocale();
    }

    @NotNull
    public static FXContext snapshot(@NotNull FXContext original) {
        return copy(original);
    }
}
