package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FXContextFactory {
    public static final String LC_ERROR_NO_HOST_SERVICES = "error.no-host-services";
    public static final String LC_ERROR_NO_PARAMETERS = "error.no-parameters";
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

    private static FXContextImpl getContext() {
        return context.get();
    }

    public static FXContext currentContext() {
        return getContext();
    }

    public static FXContext forLocale(FXContext original, Locale locale) {
        FXContextImpl fxContext = copy(original);
        fxContext.setLocale(locale);
        return fxContext;
    }

    private static FXContextImpl copy(FXContext original) {
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

    public static void initializeContext(Application application,
                                         Class<? extends FXApplication> appClass) {
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

    public static FXContext snapshot(FXContext original) {
        return copy(original);
    }

    public static void initializeStage(Stage stage) {
        getContext().setStage(stage);
    }
}
