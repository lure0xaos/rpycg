package gargoyle.rpycg.fx;

import gargoyle.rpycg.fx.log.FXLog;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

final class FXIntUtil {
    private FXIntUtil() {
        throw new IllegalStateException(FXIntUtil.class.getName());
    }

    public static Optional<URL> findResource(final Class<?> baseClass, final Locale locale,
                                             final String baseName, final String suffixes) {
        final ClassLoader classLoader = baseClass.getClassLoader();
        final ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        final List<Locale> candidateLocales = control.getCandidateLocales(baseName, locale);
        for (final String suffix : suffixes.split(",")) {
            for (final Locale specificLocale : candidateLocales) {
                final String bundleName = control.toBundleName(baseName, specificLocale);
                final String resourceName = control.toResourceName(bundleName, suffix);
                final String absoluteResourceName = !resourceName.isEmpty()
                        && '/' == resourceName.charAt(0) ? resourceName : '/' + resourceName;
                for (final URL url : new URL[]{baseClass.getResource(resourceName), classLoader.getResource(resourceName),
                        baseClass.getResource(absoluteResourceName), classLoader.getResource(absoluteResourceName)}) {
                    if (null != url) {
//                        FXLog.info("found ({0}){1}.{2} (from {3})", baseClass.getName(), baseName, suffix, resourceName);
                        return Optional.of(url);
                    }
                }
            }
        }
        FXLog.warn(FXConstants.MSG_ERROR_NO_RESOURCE, baseName, suffixes);
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(final String className, final ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(className, false, classLoader);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("could not load class %s" + className, e);
        }
    }

    public static <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(final FXContext context,
                                                                                  final String baseName,
                                                                                  final C controller, final V root) {
        return findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_FXML)
                .map(location -> {
                    final FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setCharset(context.getCharset());
                    fxmlLoader.setClassLoader(context.getBaseClass().getClassLoader());
                    fxmlLoader.setControllerFactory(context::registerAndGet);
                    loadResources(context.getBaseClass(), context.getLocale(), baseName).ifPresent(fxmlLoader::setResources);
                    fxmlLoader.setLocation(location);
                    return fxmlLoader;
                }).map(fxmlLoader -> {
                    Optional.ofNullable(controller).ifPresent(fxmlLoader::setController);
                    Optional.ofNullable(root).ifPresent(fxmlLoader::setRoot);
                    try {
                        //noinspection RedundantTypeArguments
                        final V view = fxmlLoader.<V>load();
                        prepareView(context, baseName, view.sceneProperty(), FXUtil.findScene(view).orElse(null));
                        return Optional.of(view).<FXComponent<C, V>>map(parent ->
                                        new FXComponent<>(context, fxmlLoader.getLocation(), baseName,
                                                fxmlLoader.getController(), parent))
                                .orElse(null);
                    } catch (final IOException e) {
                        FXLog.error(e, FXConstants.MSG_ERROR_LOADING_COMPONENT, baseName);
                        return null;
                    }
                });
    }

    public static <D extends Dialog<?>, V extends Parent> Optional<FXComponent<D, V>> loadDialog(final FXContext context,
                                                                                                 final D dialog) {
        final Optional<FXComponent<D, V>> component = loadComponent(context, FXUtil.resolveBaseName(dialog.getClass()),
                dialog, null);
        component.ifPresent(content -> dialog.getDialogPane().setContent(content.getView()));
        return component;
    }

    public static Optional<ResourceBundle> loadResources(final Class<?> baseClass, final Locale locale,
                                                         final String baseName) {
        try {
            return Optional.ofNullable(ResourceBundle.getBundle(baseName, locale, baseClass.getClassLoader()));
        } catch (final MissingResourceException e) {
            FXLog.error(FXConstants.MSG_ERROR_NO_RESOURCES, baseName);
            return Optional.empty();
        }
    }

    public static <T> T newInstance(final Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (final InstantiationException e) {
            throw new FXException(type + " instantiation error", e);
        } catch (final IllegalAccessException e) {
            throw new FXException(type + " access error", e);
        } catch (final NoSuchMethodException e) {
            throw new FXException(type + " construction error", e);
        } catch (final InvocationTargetException e) {
            throw new FXException(type + " invocation error", e);
        }
    }

    public static void prepareStage(final FXContext context, final String baseName, final Window window) {
        prepareView(context, baseName, window.sceneProperty(), FXUtil.findScene(window).orElse(null));
        findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT__IMAGES)
                .map(URL::toExternalForm).map(Image::new).ifPresent(image ->
                        Optional.of(window).filter(Stage.class::isInstance).map(Stage.class::cast).map(Stage::getIcons)
                                .ifPresent(images -> images.add(image)));
    }

    public static <T> Collector<T, ?, T> toSingleton(final Supplier<? extends RuntimeException> exceptionSupplier) {
        return Collectors.collectingAndThen(Collectors.toList(), list -> {
            if (1 == list.size()) return list.get(0);
            throw exceptionSupplier.get();
        });
    }

    private static void prepareView(final FXContext context, final String baseName,
                                    final ObservableValue<? extends Scene> sceneProperty, final Scene scene) {
        sceneProperty.addListener((observable, oldValue, newValue) -> {
            findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_CSS)
                    .map(URL::toExternalForm).ifPresent(url -> {
                        Optional.ofNullable(oldValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.remove(url));
                        Optional.ofNullable(newValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.add(url));
                    });
            Optional.ofNullable(context.getSkin()).filter(skin -> !skin.isEmpty()).map(skin ->
                            MessageFormat.format("{0}_{1}", baseName, skin))
                    .flatMap(skin ->
                            findResource(context.getBaseClass(), context.getLocale(), skin, FXConstants.EXT_CSS))
                    .map(URL::toExternalForm).ifPresent(url -> {
                        Optional.ofNullable(oldValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.remove(url));
                        Optional.ofNullable(newValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.add(url));
                    });
        });
        findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_CSS)
                .map(URL::toExternalForm).ifPresent(url -> Optional.ofNullable(scene).map(Scene::getStylesheets)
                        .ifPresent(strings -> strings.add(url)));
        Optional.ofNullable(context.getSkin()).filter(skin -> !skin.isEmpty()).map(skin ->
                        MessageFormat.format("{0}_{1}", baseName, skin))
                .flatMap(skin -> findResource(context.getBaseClass(), context.getLocale(), skin, FXConstants.EXT_CSS))
                .map(URL::toExternalForm).ifPresent(url ->
                        Optional.ofNullable(scene).map(Scene::getStylesheets).ifPresent(strings -> strings.add(url)));
    }

}
