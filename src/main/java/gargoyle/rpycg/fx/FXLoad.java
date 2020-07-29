package gargoyle.rpycg.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
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
    public static final String CSS = "css";
    public static final String FXML = "fxml";
    public static final String GIF = "gif";
    public static final String IMAGES = "png,gif,jpg";
    public static final String JPG = "jpg";
    public static final String PNG = "png";
    public static final String PROPERTIES = "properties";
    private static final Logger log = LoggerFactory.getLogger(FXLoad.class);

    private FXLoad() {
    }

    @NotNull
    public static <T extends Parent> Optional<T> loadComponent(@NotNull FXContext context, @NotNull String baseName,
                                                               @Nullable Object controller) {
        return loadComponent(context, baseName, controller, null);
    }

    @NotNull
    public static <T extends Parent> Optional<T> loadComponent(@NotNull FXContext context, @NotNull String baseName,
                                                               @Nullable Object controller,
                                                               @Nullable Parent root) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setCharset(context.getCharset());
        fxmlLoader.setClassLoader(context.getClassLoader());
        if (controller != null) {
            fxmlLoader.setController(controller);
        }
        if (root != null) {
            fxmlLoader.setRoot(root);
        }
        loadResources(context, baseName).ifPresent(fxmlLoader::setResources);
        findResource(context, baseName, baseName, FXML).ifPresent(fxmlLoader::setLocation);
        try {
            T view = fxmlLoader.load();
            findResource(context, baseName, baseName, CSS)
                    .ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
            return Optional.ofNullable(view);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }

    @NotNull
    public static Optional<ResourceBundle> loadResources(@NotNull FXContext context, @NotNull String baseName) {
        try {
            return Optional.ofNullable(ResourceBundle.getBundle(baseName,
                    context.getLocale(), context.getClassLoader()));
        } catch (MissingResourceException e) {
            log.error(e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }

    @NotNull
    public static Optional<URL> findResource(@NotNull FXContext context,
                                             @NotNull String baseName,
                                             @NotNull String... suffixes) {
        Locale locale = context.getLocale();
        ClassLoader classLoader = context.getClassLoader();
        for (String subsuffixes : suffixes) {
            for (String suffix : subsuffixes.split(",")) {
                Control control = Control.getControl(Control.FORMAT_DEFAULT);
                for (Locale specificLocale : control.getCandidateLocales(baseName, locale)) {
                    URL url = classLoader.getResource(
                            control.toResourceName(control.toBundleName(baseName, specificLocale), suffix));
                    if (url != null) {
                        return Optional.of(url);
                    }
                }
            }
        }
        log.error("cannot find {}{}", baseName, suffixes);
        return Optional.empty();
    }

    @NotNull
    public static <T extends Parent> Optional<T> loadComponent(@NotNull FXContext context, @NotNull String baseName) {
        return loadComponent(context, baseName, null, null);
    }

    @NotNull
    public static <T extends Parent> Optional<T> loadDialog(@NotNull FXContext context, @NotNull Dialog<?> dialog) {
        Optional<T> optional = loadComponent(context, getBaseName(dialog.getClass()), dialog, null);
        optional.ifPresent(root -> dialog.getDialogPane().setContent(root));
        return optional;
    }

    @NotNull
    public static String getBaseName(@NotNull Class<?> aClass) {
        return getBaseName(aClass, aClass.getSimpleName());
    }

    @NotNull
    public static String getBaseName(@NotNull Class<?> aClass, @NotNull String baseName) {
        if (baseName.isEmpty()) {
            return "";
        }
        return baseName.charAt(0) == '/' ? baseName.substring(1)
                : aClass.getPackage().getName().replace('.', '/') + '/' + baseName;
    }
}
