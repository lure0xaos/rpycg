package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public interface FXContext {

    @NotNull Optional<URL> findResource(@NotNull String baseName, @NotNull String suffix);

    @NotNull
    default Optional<URL> findResource(@NotNull FXComponent<?, ?> component, @NotNull String suffix) {
        return findResource(component.getBaseName(), suffix);
    }

    @NotNull String getBaseName(@NotNull String baseName, @NotNull String name);

    @NotNull String getBaseName(@NotNull Class<?> aClass, @NotNull String baseName);

    @NotNull
    default String getBaseName(@NotNull Class<?> aClass) {
        return getBaseName(aClass, aClass.getSimpleName());
    }

    @NotNull <T> T getBean(@NotNull Class<? extends T> type);

    @NotNull
    Charset getCharset();

    @NotNull
    ClassLoader getClassLoader();

    @Nullable
    HostServices getHostServices();

    @NotNull
    Locale getLocale();

    @Nullable
    Application.Parameters getParameters();

    @Nullable
    Preferences getPreferences();

    @Nullable
    String getSkin();

    @NotNull <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull Class<? extends C> componentClass);

    @NotNull
    default <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull C component) {
        return loadComponent(getBaseName(component.getClass()), component, component);
    }

    @NotNull <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull String baseName,
                                                                             @Nullable C controller,
                                                                             @Nullable V root);

    @NotNull
    default <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull String baseName) {
        return loadComponent(baseName, this::getBean);
    }

    @NotNull <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull String baseName,
            Callback<Class<?>, Object> controllerFactory);

    @NotNull <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(@NotNull D dialog);

    @NotNull
    default Optional<ResourceBundle> loadResources(@NotNull Class<?> aClass) {
        return loadResources(getBaseName(aClass));
    }

    @NotNull Optional<ResourceBundle> loadResources(@NotNull String baseName);

    @NotNull
    default Optional<ResourceBundle> loadResources(@NotNull FXComponent<?, ?> component) {
        return loadResources(component.getBaseName());
    }
}
