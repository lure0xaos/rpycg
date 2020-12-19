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
    private final Property<Charset> charset;
    private final Property<ClassLoader> classLoader;
    private final Property<HostServices> hostServices;
    private final Property<Locale> locale;
    private final LocaleConverter localeConverter;
    private final Property<Application.Parameters> parameters;
    private final Property<Preferences> preferences;
    private final Property<String> skin;

    FXContextImpl(Charset charset, Locale locale, ClassLoader classLoader) {
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

    public Property<Charset> charsetProperty() {
        return charset;
    }

    public Property<ClassLoader> classLoaderProperty() {
        return classLoader;
    }

    private <C, V extends Parent> Optional<FXComponent<C, V>> createComponent(FXMLLoader fxmlLoader,
                                                                              String baseName,
                                                                              V view) {
        FXContext context = this;
        findResource(baseName, FXConstants.EXT_CSS).ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        Optional.ofNullable(context.getSkin()).map(skin -> baseName + '_' + skin)
                .flatMap(skin -> findResource(skin, FXConstants.EXT_CSS))
                .ifPresent(url -> view.getStylesheets().add(url.toExternalForm()));
        return Optional.of(view).map(parent ->
                new FXComponent<>(context, fxmlLoader.getLocation(), baseName, fxmlLoader.getController(), parent));
    }

    private Optional<FXMLLoader> createFxmlLoader(String baseName,
                                                  Callback<Class<?>, Object> controllerFactory) {
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
    public Optional<URL> findResource(String baseName, String suffix) {
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

    @Override
    public String getBaseName(String baseName, String name) {
        if (baseName.isEmpty()) {
            return "";
        }
        if (name.isEmpty()) {
            return "";
        }
        return name.charAt(0) == '/' ? name.substring(1) :
                baseName.substring(0, baseName.lastIndexOf('/')) + '/' + name;
    }

    @Override
    public Charset getCharset() {
        return charset.getValue();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader.getValue();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader.setValue(classLoader);
    }

    @Override
    public HostServices getHostServices() {
        return hostServices.getValue();
    }

    @Override
    public Locale getLocale() {
        return locale.getValue();
    }

    public void setLocale(Locale locale) {
        this.locale.setValue(locale);
    }

    @Override
    public Application.Parameters getParameters() {
        return parameters.getValue();
    }

    public void setParameters(Application.Parameters parameters) {
        this.parameters.setValue(parameters);
    }

    @Override
    public Preferences getPreferences() {
        return preferences.getValue();
    }

    @Override
    public String getSkin() {
        return skin.getValue();
    }

    @Override
    public <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            Class<? extends C> componentClass) {
        try {
            return loadComponent(getBean(componentClass));
        } catch (FXException e) {
            log.error(FXConstants.MSG_ERROR_LOADING_COMPONENT, componentClass, e);
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    public <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(String baseName,
                                                                           C controller,
                                                                           V root) {
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
    public String getBaseName(Class<?> aClass, String baseName) {
        if (baseName.isEmpty()) {
            return "";
        }
        return baseName.charAt(0) == '/' ? baseName.substring(1)
                : aClass.getPackage().getName().replace('.', '/') + '/' + baseName;
    }

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    public <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            String baseName,
            Callback<Class<?>, Object> controllerFactory) {
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

    @Override
    public <T> T getBean(Class<? extends T> type) {
        return FXReflection.instantiate(type);
    }

    @Override
    public <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(D dialog) {
        Optional<FXComponent<D, V>> component =
                loadComponent(getBaseName(dialog.getClass()), dialog, null);
        component.ifPresent(content -> dialog.getDialogPane().setContent(content.getView()));
        return component;
    }

    @Override
    public Optional<ResourceBundle> loadResources(String baseName) {
        FXContext context = this;
        try {
            return Optional.ofNullable(ResourceBundle.getBundle(baseName,
                    context.getLocale(), context.getClassLoader()));
        } catch (MissingResourceException e) {
            log.error(FXConstants.MSG_ERROR_NO_RESOURCES, baseName);
            return Optional.empty();
        }
    }

    public void setSkin(String skin) {
        this.skin.setValue(skin);
    }

    public void setPreferences(Preferences preferences) {
        this.preferences.setValue(preferences);
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices.setValue(hostServices);
    }

    public void setCharset(Charset charset) {
        this.charset.setValue(charset);
    }

    public Property<HostServices> hostServicesProperty() {
        return hostServices;
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

    void initializeSkin(Preferences prefs, Application.Parameters appParameters) {
        skin.setValue(prefs.get(PREF_SKIN, appParameters.getNamed().get(CMD_SKIN)));
    }

    public Property<Locale> localeProperty() {
        return locale;
    }

    public Property<Application.Parameters> parametersProperty() {
        return parameters;
    }

    public Property<Preferences> preferencesProperty() {
        return preferences;
    }

    public Property<String> skinProperty() {
        return skin;
    }
}
