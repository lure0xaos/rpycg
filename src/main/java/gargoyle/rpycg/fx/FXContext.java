package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public interface FXContext {
    default Optional<URL> findResource(FXComponent<?, ?> component, String suffix) {
        return findResource(component.getBaseName(), suffix);
    }

    Optional<URL> findResource(String baseName, String suffix);

    String getBaseName(String baseName, String name);

    Charset getCharset();

    ClassLoader getClassLoader();

    HostServices getHostServices();

    Locale getLocale();

    Application.Parameters getParameters();

    Preferences getPreferences();

    String getSkin();

    Stage getStage();

    <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            Class<? extends C> componentClass);

    default <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(C component) {
        return loadComponent(getBaseName(component.getClass()), component, component);
    }

    <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(String baseName,
                                                                    C controller,
                                                                    V root);

    default String getBaseName(Class<?> aClass) {
        return getBaseName(aClass, aClass.getSimpleName());
    }

    String getBaseName(Class<?> aClass, String baseName);

    default <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(String baseName) {
        return loadComponent(baseName, this::getBean);
    }

    <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            String baseName,
            Callback<Class<?>, Object> controllerFactory);

    <T> T getBean(Class<? extends T> type);

    <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(D dialog);

    default Optional<ResourceBundle> loadResources(Class<?> aClass) {
        return loadResources(getBaseName(aClass));
    }

    Optional<ResourceBundle> loadResources(String baseName);

    default Optional<ResourceBundle> loadResources(FXComponent<?, ?> component) {
        return loadResources(component.getBaseName());
    }
}
