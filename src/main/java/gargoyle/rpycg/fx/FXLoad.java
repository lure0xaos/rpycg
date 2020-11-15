package gargoyle.rpycg.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public final class FXLoad {
    public static final String EXT_CSS = "css";
    public static final String EXT_GIF = "gif";
    public static final String EXT_JPG = "jpg";
    public static final String EXT_PNG = "png";
    public static final String EXT_IMAGES = String.join(",", EXT_PNG, EXT_GIF, EXT_JPG);
    public static final String MSG_ERROR_LOADING_COMPONENT = "Error loading component {}";
    public static final String MSG_ERROR_NO_RESOURCE = "Can''t find resources for {}[{}]";
    public static final String MSG_ERROR_NO_RESOURCES = "Can''t find resources for {}";
    private static final String EXT_FXML = "fxml";
    private static final Logger log = LoggerFactory.getLogger(FXLoad.class);

    private FXLoad() {
        throw new IllegalStateException(getClass().getName());
    }

    @NotNull
    public static Optional<URL> findResource(@NotNull FXComponent<?, ?> component, @NotNull String suffix) {
        return findResource(FXContextFactory.currentContext(), component, suffix);
    }

    @NotNull
    public static Optional<URL> findResource(@NotNull FXContext context,
                                             @NotNull FXComponent<?, ?> component, @NotNull String suffix) {
        return findResource(context, component.getBaseName(), suffix);
    }

    @NotNull
    public static Optional<URL> findResource(@NotNull FXContext context,
                                             @NotNull String baseName, @NotNull String suffix) {
        Locale locale = context.getLocale();
        ClassLoader classLoader = context.getClassLoader();
        for (String subSuffix : suffix.split(",")) {
            Control control = Control.getControl(Control.FORMAT_DEFAULT);
            for (Locale specificLocale : control.getCandidateLocales(baseName, locale)) {
                URL url = classLoader.getResource(
                        control.toResourceName(control.toBundleName(baseName, specificLocale), subSuffix));
                if (url != null) {
                    return Optional.of(url);
                }
            }
        }
        log.error(MSG_ERROR_NO_RESOURCE, baseName, suffix);
        return Optional.empty();
    }

    @NotNull
    public static Optional<URL> findResource(@NotNull String baseName, @NotNull String suffix) {
        return findResource(FXContextFactory.currentContext(), baseName, suffix);
    }

    @NotNull
    public static String getBaseName(@NotNull String baseName, @NotNull String name) {
        if (baseName.isEmpty()) {
            return "";
        }
        if (name.isEmpty()) {
            return "";
        }
        return name.charAt(0) == '/' ? name.substring(1) :
                baseName.substring(0, baseName.lastIndexOf('/')) + '/' + name;
    }

    @NotNull
    public static <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull Class<? extends C> componentClass) {
        return loadComponent(FXContextFactory.currentContext(), componentClass);
    }

    @NotNull
    public static String getBaseName(@NotNull Class<?> aClass, @NotNull String baseName) {
        if (baseName.isEmpty()) {
            return "";
        }
        return baseName.charAt(0) == '/' ? baseName.substring(1)
                : aClass.getPackage().getName().replace('.', '/') + '/' + baseName;
    }

    @NotNull
    public static <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull FXContext context,
                                                                                            @NotNull C component) {
        return FXLoad.loadComponent(context, getBaseName(component.getClass()), component, component);
    }

    @SuppressWarnings("RedundantTypeArguments")
    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull FXContext context,
            @NotNull String baseName,
            @NotNull Callback<Class<?>, Object> controllerFactory) {
        return createFxmlLoader(context, baseName, controllerFactory)
                .map(fxmlLoader -> {
                    try {
                        return FXLoad.<C, V>createComponent(fxmlLoader, context, baseName, fxmlLoader.<V>load())
                                .orElse(null);
                    } catch (IOException e) {
                        log.error(MSG_ERROR_LOADING_COMPONENT, baseName, e);
                        return null;
                    }
                });
    }

    @NotNull
    public static String getBaseName(@NotNull Class<?> aClass) {
        return getBaseName(aClass, aClass.getSimpleName());
    }

    @NotNull
    private static <C, V extends Parent> Optional<FXComponent<C, V>> createComponent(@NotNull FXMLLoader fxmlLoader,
                                                                                     @NotNull FXContext context,
                                                                                     @NotNull String baseName,
                                                                                     @NotNull V view) {
        findResource(context, baseName, EXT_CSS).ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        Optional.ofNullable(context.getSkin()).map(skin -> baseName + '_' + skin)
                .flatMap(skin -> findResource(context, skin, EXT_CSS))
                .ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        return Optional.of(view).map(parent ->
                new FXComponent<>(context, fxmlLoader.getLocation(), baseName, fxmlLoader.getController(), parent));
    }

    @NotNull
    private static Optional<FXMLLoader> createFxmlLoader(@NotNull FXContext context,
                                                         @NotNull String baseName,
                                                         @NotNull Callback<Class<?>, Object> controllerFactory) {
        return findResource(context, baseName, EXT_FXML).map(location -> {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setCharset(context.getCharset());
            fxmlLoader.setClassLoader(context.getClassLoader());
            fxmlLoader.setControllerFactory(controllerFactory);
            loadResources(context, baseName).ifPresent(fxmlLoader::setResources);
            fxmlLoader.setLocation(location);
            return fxmlLoader;
        });
    }

    @NotNull
    public static <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull FXContext context,
            @NotNull Class<? extends C> componentClass) {
        try {
            return FXLoad.loadComponent(context, FXReflection.instantiate(componentClass));
        } catch (FXException e) {
            log.error(MSG_ERROR_LOADING_COMPONENT, componentClass, e);
            return Optional.empty();
        }
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull FXContext context, @NotNull String baseName) {
        try {
            return Optional.ofNullable(ResourceBundle.getBundle(baseName,
                    context.getLocale(), context.getClassLoader()));
        } catch (MissingResourceException e) {
            log.error(MSG_ERROR_NO_RESOURCES, baseName);
            return Optional.empty();
        }
    }

    @NotNull
    public static <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull C component) {
        return loadComponent(FXContextFactory.currentContext(), component);
    }

    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull String baseName,
                                                                                  @Nullable C controller,
                                                                                  @Nullable V root) {
        return loadComponent(FXContextFactory.currentContext(), baseName, controller, root);
    }

    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull String baseName) {
        return loadComponent(FXContextFactory.currentContext(), baseName);
    }

    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull FXContext context,
                                                                                  @NotNull String baseName) {
        return loadComponent(context, baseName, FXReflection::instantiate);
    }

    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull String baseName,
            Callback<Class<?>, Object> controllerFactory) {
        return loadComponent(FXContextFactory.currentContext(), baseName, controllerFactory);
    }

    public static <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(@NotNull FXContext context, @NotNull D dialog) {
        Optional<FXComponent<D, V>> component =
                loadComponent(context, getBaseName(dialog.getClass()), dialog, null);
        component.ifPresent(content -> dialog.getDialogPane().setContent(content.getView()));
        return component;
    }

    public static <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(@NotNull D dialog) {
        return loadDialog(FXContextFactory.currentContext(), dialog);
    }

    @SuppressWarnings("RedundantTypeArguments")
    @NotNull
    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull FXContext context,
                                                                                  @NotNull String baseName,
                                                                                  @Nullable C controller,
                                                                                  @Nullable V root) {

        return createFxmlLoader(context, baseName, FXReflection::instantiate)
                .map(fxmlLoader -> {
                    Optional.ofNullable(controller).ifPresent(fxmlLoader::setController);
                    Optional.ofNullable(root).ifPresent(fxmlLoader::setRoot);
                    try {
                        return FXLoad.<C, V>createComponent(fxmlLoader, context, baseName, fxmlLoader.<V>load())
                                .orElse(null);
                    } catch (IOException e) {
                        log.error(MSG_ERROR_LOADING_COMPONENT, baseName, e);
                        return null;
                    }
                });
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull Class<?> aClass) {
        return loadResources(FXContextFactory.currentContext(), aClass);
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull FXContext context, @NotNull Class<?> aClass) {
        return loadResources(context, getBaseName(aClass));
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull String baseName) {
        return loadResources(FXContextFactory.currentContext(), baseName);
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull FXComponent<?, ?> component) {
        return loadResources(FXContextFactory.currentContext(), component);
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull FXContext context,
                                                         @NotNull FXComponent<?, ?> component) {
        return loadResources(context, component.getBaseName());
    }
}
