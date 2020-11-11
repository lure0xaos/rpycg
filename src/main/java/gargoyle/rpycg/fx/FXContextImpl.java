package gargoyle.rpycg.fx;

import gargoyle.rpycg.service.LocaleConverter;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.prefs.Preferences;

final class FXContextImpl implements FXContext {

    private static final String CMD_SKIN = "skin";
    private static final String PREF_LOCALE = "locale";
    private static final String PREF_SKIN = "skin";

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

    @NotNull
    public Property<ClassLoader> classLoaderProperty() {
        return classLoader;
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

    @NotNull
    public Property<HostServices> hostServicesProperty() {
        return hostServices;
    }

    void initializeLocale() {
        if (getPreferences() != null) {
            setLocale(localeConverter.toLocale(getPreferences().get(PREF_LOCALE, localeConverter.toString(getLocale()))));
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

    @NotNull
    public Property<Preferences> preferencesProperty() {
        return preferences;
    }

    @NotNull
    public Property<String> skinProperty() {
        return skin;
    }
}
