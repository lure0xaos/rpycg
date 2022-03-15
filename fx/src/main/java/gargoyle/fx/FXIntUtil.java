package gargoyle.fx;

import gargoyle.fx.log.FXLog;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Optional;

final class FXIntUtil {
    private FXIntUtil() {
        throw new IllegalStateException(FXIntUtil.class.getName());
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
        return FXUtil.findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_FXML)
                .map(location -> {
                    final FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setCharset(context.getCharset());
                    fxmlLoader.setClassLoader(context.getBaseClass().getClassLoader());
                    fxmlLoader.setControllerFactory(context::registerAndGet);
                    FXUtil.loadResources(context.getBaseClass(), context.getLocale(), baseName).ifPresent(fxmlLoader::setResources);
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
                        FXLog.error(e, "Error loading component {baseName}", Map.of("baseName", baseName));
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

    public static <T> T newInstance(final Class<T> type) {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<T>) () -> type.getConstructor().newInstance());
        } catch (final PrivilegedActionException e) {
            throw new FXException(type + " instantiation error", e.getCause().getCause());
        }
    }

    static void prepareStage(final FXContext context, final String baseName, final Window window) {
        prepareView(context, baseName, window.sceneProperty(), FXUtil.findScene(window).orElse(null));
        FXUtil.findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT__IMAGES)
                .map(URL::toExternalForm).map(Image::new).ifPresent(image ->
                        Optional.ofNullable(window).filter(Stage.class::isInstance).map(Stage.class::cast).map(Stage::getIcons)
                                .ifPresent(images -> images.add(image)));
    }

    private static void prepareView(final FXContext context, final String baseName,
                                    final ObservableValue<? extends Scene> sceneProperty, final Scene scene) {
        sceneProperty.addListener((observable, oldValue, newValue) -> {
            FXUtil.findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_CSS)
                    .map(URL::toExternalForm).ifPresent(url -> {
                        Optional.ofNullable(oldValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.remove(url));
                        Optional.ofNullable(newValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.add(url));
                    });
            Optional.ofNullable(context.getSkin()).filter(skin -> !skin.isEmpty()).map(skin ->
                            FXUtil.format("{baseName}_{skin}", Map.of("baseName", baseName, "skin", skin)))
                    .flatMap(skin ->
                            FXUtil.findResource(context.getBaseClass(), context.getLocale(), skin, FXConstants.EXT_CSS))
                    .map(URL::toExternalForm).ifPresent(url -> {
                        Optional.ofNullable(oldValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.remove(url));
                        Optional.ofNullable(newValue).map(Scene::getStylesheets).ifPresent(stylesheets ->
                                stylesheets.add(url));
                    });
        });
        FXUtil.findResource(context.getBaseClass(), context.getLocale(), baseName, FXConstants.EXT_CSS)
                .map(URL::toExternalForm).ifPresent(url -> Optional.ofNullable(scene).map(Scene::getStylesheets)
                        .ifPresent(strings -> strings.add(url)));
        Optional.ofNullable(context.getSkin()).filter(skin -> !skin.isEmpty()).map(skin ->
                        FXUtil.format("{baseName}_{skin}", Map.of("baseName", baseName, "skin", skin)))
                .flatMap(skin -> FXUtil.findResource(context.getBaseClass(), context.getLocale(), skin, FXConstants.EXT_CSS))
                .map(URL::toExternalForm).ifPresent(url ->
                        Optional.ofNullable(scene).map(Scene::getStylesheets).ifPresent(strings -> strings.add(url)));
    }

}
