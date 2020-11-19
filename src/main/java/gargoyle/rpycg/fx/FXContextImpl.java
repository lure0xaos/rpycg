package gargoyle.rpycg.fx;

import gargoyle.rpycg.service.LocaleConverter;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
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
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

final class FXContextImpl implements FXContext {

    private static final String CMD_SKIN = "skin";
    private static final String PREF_LOCALE = "locale";
    private static final String PREF_SKIN = "skin";
    private static final Logger log = LoggerFactory.getLogger(FXContextImpl.class);
    @NotNull
    private final Property<Charset> charset;
    @NotNull
    private final Property<ClassLoader> classLoader;
    @NotNull
    private final Property<HostServices> hostServices;
    @NotNull
    private final Property<Locale> locale;
    private final LocaleConverter localeConverter;
    @NotNull
    private final Property<Application.Parameters> parameters;
    @NotNull
    private final Property<Preferences> preferences;
    @NotNull
    private final Property<String> skin;

    FXContextImpl(@NotNull Charset charset, @NotNull Locale locale, @NotNull ClassLoader classLoader) {
        this.locale = new SimpleObjectProperty<>(locale);
        this.charset = new SimpleObjectProperty<>(charset);
        this.classLoader = new SimpleObjectProperty<>(classLoader);
        localeConverter = new LocaleConverter(this);
        this.locale.setValue(localeConverter.getSimilarLocale(localeConverter.getLocales(), locale));
        this.locale.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && getPreferences() != null) {
                getPreferences().put(PREF_LOCALE, localeConverter.toString(newValue));
            }
        });
        hostServices = new SimpleObjectProperty<>();
        parameters = new SimpleObjectProperty<>();
        preferences = new SimpleObjectProperty<>();
        skin = new SimpleObjectProperty<>();
    }

    @NotNull
    public Property<Charset> charsetProperty() {
        return charset;
    }

    @Override
    public <T> @NotNull T getBean(@NotNull Class<? extends T> type) {
        return FXReflection.instantiate(type);
    }

    @NotNull
    public Property<ClassLoader> classLoaderProperty() {
        return classLoader;
    }

    @Override
    @NotNull
    public Optional<URL> findResource(@NotNull String baseName, @NotNull String suffix) {
        Locale locale = getLocale();
        ClassLoader classLoader = getClassLoader();
        for (String subSuffix : suffix.split(",")) {
            ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
            for (Locale specificLocale : control.getCandidateLocales(baseName, locale)) {
                URL url = classLoader.getResource(
                        control.toResourceName(control.toBundleName(baseName, specificLocale), subSuffix));
                if (url != null) {
                    return Optional.of(url);
                }
            }
        }
        log.error(FXConstants.MSG_ERROR_NO_RESOURCE, baseName, suffix);
        return Optional.empty();
    }

    @NotNull
    private <C, V extends Parent> Optional<FXComponent<C, V>> createComponent(@NotNull FXMLLoader fxmlLoader,
                                                                              @NotNull String baseName,
                                                                              @NotNull V view) {
        FXContext context = this;
        findResource(baseName, FXConstants.EXT_CSS).ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        Optional.ofNullable(context.getSkin()).map(skin -> baseName + '_' + skin)
                .flatMap(skin -> findResource(skin, FXConstants.EXT_CSS))
                .ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        return Optional.of(view).map(parent ->
                new FXComponent<>(context, fxmlLoader.getLocation(), baseName, fxmlLoader.getController(), parent));
    }

    @Override
    @NotNull
    public String getBaseName(@NotNull String baseName, @NotNull String name) {
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
    private Optional<FXMLLoader> createFxmlLoader(@NotNull String baseName,
                                                  @NotNull Callback<Class<?>, Object> controllerFactory) {
        FXContext context = this;
        return findResource(baseName, FXConstants.EXT_FXML).map(location -> {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setCharset(context.getCharset());
            fxmlLoader.setClassLoader(context.getClassLoader());
            fxmlLoader.setControllerFactory(controllerFactory);
            loadResources(baseName).ifPresent(fxmlLoader::setResources);
            fxmlLoader.setLocation(location);
            return fxmlLoader;
        });
    }

    @Override
    @NotNull
    public String getBaseName(@NotNull Class<?> aClass, @NotNull String baseName) {
        if (baseName.isEmpty()) {
            return "";
        }
        return baseName.charAt(0) == '/' ? baseName.substring(1)
                : aClass.getPackage().getName().replace('.', '/') + '/' + baseName;
    }

    @NotNull
    public Property<HostServices> hostServicesProperty() {
        return hostServices;
    }

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    @NotNull
    public <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull String baseName,
            @NotNull Callback<Class<?>, Object> controllerFactory) {
        return createFxmlLoader(baseName, controllerFactory)
                .map(fxmlLoader -> {
                    try {
                        return this.<C, V>createComponent(fxmlLoader, baseName, fxmlLoader.<V>load())
                                .orElse(null);
                    } catch (IOException e) {
                        log.error(FXConstants.MSG_ERROR_LOADING_COMPONENT, baseName, e);
                        return null;
                    }
                });
    }

    void initializeLocale() {
        if (getPreferences() != null) {
            setLocale(localeConverter.toLocale(
                    getPreferences().get(PREF_LOCALE, localeConverter.toString(getLocale()))));
        }
    }

    @SuppressWarnings("AccessOfSystemProperties")
    void initializePreferences(Class<? extends FXApplication> applicationClass) {
        System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
        preferences.setValue(Preferences.userNodeForPackage(applicationClass));
    }

    void initializeSkin(@NotNull Preferences prefs, @NotNull Application.Parameters appParameters) {
        skin.setValue(prefs.get(PREF_SKIN, appParameters.getNamed().get(CMD_SKIN)));
    }

    @NotNull
    public Property<Locale> localeProperty() {
        return locale;
    }

    @NotNull
    public Property<Application.Parameters> parametersProperty() {
        return parameters;
    }

    @Override
    @NotNull
    public <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            @NotNull Class<? extends C> componentClass) {
        try {
            return loadComponent(getBean(componentClass));
        } catch (FXException e) {
            log.error(FXConstants.MSG_ERROR_LOADING_COMPONENT, componentClass, e);
            return Optional.empty();
        }
    }

    @NotNull
    public Property<Preferences> preferencesProperty() {
        return preferences;
    }

    @Override
    @NotNull
    public Optional<ResourceBundle> loadResources(@NotNull String baseName) {
        FXContext context = this;
        try {
            return Optional.ofNullable(ResourceBundle.getBundle(baseName,
                    context.getLocale(), context.getClassLoader()));
        } catch (MissingResourceException e) {
            log.error(FXConstants.MSG_ERROR_NO_RESOURCES, baseName);
            return Optional.empty();
        }
    }

    @NotNull
    public Property<String> skinProperty() {
        return skin;
    }

    @Override
    public <D extends Dialog<?>, V extends Parent>
    @NotNull Optional<FXComponent<D, V>> loadDialog(@NotNull D dialog) {
        Optional<FXComponent<D, V>> component =
                loadComponent(getBaseName(dialog.getClass()), dialog, null);
        component.ifPresent(content -> dialog.getDialogPane().setContent(content.getView()));
        return component;
    }

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    @NotNull
    public <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(@NotNull String baseName,
                                                                           @Nullable C controller,
                                                                           @Nullable V root) {
        return createFxmlLoader(baseName, this::getBean)
                .map(fxmlLoader -> {
                    Optional.ofNullable(controller).ifPresent(fxmlLoader::setController);
                    Optional.ofNullable(root).ifPresent(fxmlLoader::setRoot);
                    try {
                        return this.<C, V>createComponent(fxmlLoader, baseName, fxmlLoader.<V>load())
                                .orElse(null);
                    } catch (IOException e) {
                        log.error(FXConstants.MSG_ERROR_LOADING_COMPONENT, baseName, e);
                        return null;
                    }
                });
    }

    @Override
    @NotNull
    public Charset getCharset() {
        return charset.getValue();
    }

    @Override
    @NotNull
    public ClassLoader getClassLoader() {
        return classLoader.getValue();
    }

    public void setClassLoader(@NotNull ClassLoader classLoader) {
        this.classLoader.setValue(classLoader);
    }

    @Override
    @Nullable
    public HostServices getHostServices() {
        return hostServices.getValue();
    }

    @Override
    @NotNull
    public Locale getLocale() {
        return locale.getValue();
    }

    public void setLocale(@NotNull Locale locale) {
        this.locale.setValue(locale);
    }

    @Override
    @Nullable
    public Application.Parameters getParameters() {
        return parameters.getValue();
    }

    public void setParameters(@NotNull Application.Parameters parameters) {
        this.parameters.setValue(parameters);
    }

    @Override
    @Nullable
    public Preferences getPreferences() {
        return preferences.getValue();
    }

    @Override
    @Nullable
    public String getSkin() {
        return skin.getValue();
    }

    public void setSkin(@Nullable String skin) {
        this.skin.setValue(skin);
    }

    public void setPreferences(@NotNull Preferences preferences) {
        this.preferences.setValue(preferences);
    }

    public void setHostServices(@NotNull HostServices hostServices) {
        this.hostServices.setValue(hostServices);
    }

    public void setCharset(@NotNull Charset charset) {
        this.charset.setValue(charset);
    }
}
