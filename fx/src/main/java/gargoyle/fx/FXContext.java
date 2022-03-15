package gargoyle.fx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.Closeable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public abstract class FXContext implements Closeable {

    public final Optional<ButtonType> alert(final String message, final ButtonType... buttons) {
        return FXDialogs.alert(this, getStage(), message, buttons);
    }

    public final Optional<ButtonType> alert(final Stage owner, final String message, final ButtonType... buttons) {
        return FXDialogs.alert(this, owner, message, buttons);
    }

    public final Optional<ButtonType> alert(final String message, final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.alert(this, getStage(), message, buttons);
    }

    public final Optional<ButtonType> alert(final Stage owner, final String message,
                                            final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.alert(this, owner, message, buttons);
    }

    public final ButtonType[] asButtonTypeArray(final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.asButtonTypeArray(buttons);
    }

    public final List<ButtonType> asButtonTypeCollection(final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.asButtonTypeCollection(buttons);
    }

    public final Boolean ask(final String message) {
        return FXDialogs.ask(this, getStage(), message);
    }

    public final Boolean ask(final Stage owner, final String message) {
        return FXDialogs.ask(this, owner, message);
    }

    @Override
    public final void close() {
    }

    public final boolean confirm(final String message) {
        return FXDialogs.confirm(this, getStage(), message);
    }

    public final boolean confirm(final Stage owner, final String message) {
        return FXDialogs.confirm(this, owner, message);
    }

    public final boolean confirm(final String message, final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.confirm(this, getStage(), message, buttons);
    }

    public final boolean confirm(final Stage owner, final String message,
                                 final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.confirm(this, owner, message, buttons);
    }

    public final Optional<ButtonType> confirmExt(final String message,
                                                 final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.confirmExt(this, getStage(), message, buttons);
    }

    public final Optional<ButtonType> confirmExt(final Stage owner, final String message,
                                                 final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.confirmExt(this, owner, message, buttons);
    }

    public final Hyperlink createHyperlink(final String text, final Consumer<? super Hyperlink> action) {
        return FXDialogs.createHyperlink(text, action);
    }

    public final <R> void decorateDialog(final Dialog<R> dialog, final Callback<ButtonType, R> resultConverter,
                                         final Map<ButtonBar.ButtonData, String> buttons, final String title) {
        FXDialogs.decorateDialog(this, dialog, resultConverter, buttons, title);
    }

    public final <R> void decorateDialog(final Dialog<R> dialog, final Callback<ButtonType, R> resultConverter,
                                         final Map<ButtonBar.ButtonData, String> buttons, final String title,
                                         final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator) {
        FXDialogs.decorateDialog(this, dialog, resultConverter, buttons, title, detailsButtonDecorator);
    }

    public final void decorateDialogAs(final Alert dialog, final Map<ButtonBar.ButtonData, String> buttons) {
        FXDialogs.decorateDialogAs(this, getStage(), dialog, buttons);
    }

    public final void decorateDialogAs(final Stage owner, final Alert dialog,
                                       final Map<ButtonBar.ButtonData, String> buttons) {
        FXDialogs.decorateDialogAs(this, owner, dialog, buttons);
    }

    public final Optional<ButtonType> dialog(final Alert.AlertType type, final String message,
                                             final Supplier<String> titleProvider, final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.dialog(this, getStage(), type, message, titleProvider, buttons);
    }

    public final Optional<ButtonType> dialog(final Stage owner, final Alert.AlertType type, final String message,
                                             final Supplier<String> titleProvider, final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.dialog(this, owner, type, message, titleProvider, buttons);
    }

    public final Optional<ButtonType> error(final String message, final Exception ex, final ButtonType... buttons) {
        return FXDialogs.error(this, getStage(), message, ex, buttons);
    }

    public final Optional<ButtonType> error(final Stage owner, final String message, final Exception ex,
                                            final ButtonType... buttons) {
        return FXDialogs.error(this, owner, message, ex, buttons);
    }

    public final Optional<ButtonType> error(final String message, final Exception ex,
                                            final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.error(this, getStage(), message, ex, buttons);
    }

    public final Optional<ButtonType> error(final Stage owner, final String message, final Exception ex,
                                            final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.error(this, owner, message, ex, buttons);
    }

    public final Optional<ButtonType> error(final String message, final ButtonType... buttons) {
        return FXDialogs.error(this, getStage(), message, buttons);
    }

    public final Optional<ButtonType> error(final Stage owner, final String message, final ButtonType... buttons) {
        return FXDialogs.error(this, owner, message, buttons);
    }

    public final Optional<ButtonType> error(final String message, final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.error(this, getStage(), message, buttons);
    }

    public final Optional<ButtonType> error(final Stage owner, final String message,
                                            final Map<ButtonBar.ButtonData, String> buttons) {
        return FXDialogs.error(this, owner, message, buttons);
    }

    public final Optional<URL> findResource(final FXComponent<?, ?> component, final String suffix) {
        return FXUtil.findResource(getBaseClass(), getLocale(), component.getBaseName(), suffix);
    }

    public final Optional<URL> findResource(final Class<?> baseClass, final String suffix) {
        return FXUtil.findResource(baseClass, getLocale(), FXUtil.resolveBaseName(baseClass), suffix);
    }

    public final Optional<URL> findResource(final String baseName, final String suffix) {
        return FXUtil.findResource(getBaseClass(), getLocale(), baseName, suffix);
    }

    public final Optional<URL> findResource(final Class<?> baseClass, final String baseName, final String suffix) {
        return FXUtil.findResource(baseClass, getLocale(), FXUtil.resolveBaseName(baseClass, baseName), suffix);
    }

    public abstract Class<?> getBaseClass();

    public final <T> T getBean(final Class<T> type) {
        return getRegistry().getBean(type);
    }

    public final <T> T getBean(final String name, final Class<T> type) {
        return getRegistry().getBean(name, type);
    }

    public abstract Charset getCharset();

    public final HostServices getHostServices() {
        return getBean(HostServices.class.getName(), HostServices.class);
    }

    public abstract Locale getLocale();

    public final Application.Parameters getParameters() {
        return getBean(Application.Parameters.class.getName(), Application.Parameters.class);
    }

    public abstract Preferences getPreferences();

    public abstract String getSkin();

    public final Stage getStage() {
        return getBean(Stage.class.getName(), Stage.class);
    }

    public final void initializeContext(final HostServices hostServices, final Application.Parameters parameters) {
        register(HostServices.class, hostServices);
        register(Application.Parameters.class, parameters);
    }

    public final <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(final C component) {
        return FXIntUtil.loadComponent(this, FXUtil.resolveBaseName(component.getClass()), component, component);
    }

    public final <C extends V, V extends Parent> Optional<FXComponent<C, V>> loadComponent(
            final Class<? extends C> componentClass) {
        final C component = registerAndGet(componentClass);
        return FXIntUtil.loadComponent(this, FXUtil.resolveBaseName(component.getClass()), component, component);
    }

    public final <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(final String baseName,
                                                                                 final C controller,
                                                                                 final V root) {
        return FXIntUtil.loadComponent(this, baseName, controller, root);
    }

    public final <C, V extends Parent> Optional<FXComponent<C, V>> loadComponent(final String baseName) {
        return FXIntUtil.loadComponent(this, baseName, null, null);
    }

    public final <D extends Dialog<?>, V extends Parent>
    Optional<FXComponent<D, V>> loadDialog(final D dialog) {
        return FXIntUtil.loadDialog(this, dialog);
    }

    public final Optional<ResourceBundle> loadResources(final Class<?> aClass) {
        return FXUtil.loadResources(getBaseClass(), getLocale(), FXUtil.resolveBaseName(aClass));
    }

    public final Optional<ResourceBundle> loadResources(final FXComponent<?, ?> component) {
        return FXUtil.loadResources(getBaseClass(), getLocale(), component.getBaseName());
    }

    public final Optional<ResourceBundle> loadResources(final String baseName) {
        return FXUtil.loadResources(getBaseClass(), getLocale(), baseName);
    }

    public final String prompt(final String message) {
        return FXDialogs.prompt(this, getStage(), message);
    }

    public final String prompt(final Stage owner, final String message) {
        return FXDialogs.prompt(this, owner, message);
    }

    public final String resolveBaseName(final String baseName, final String name) {
        return FXUtil.resolveBaseName(baseName, name);
    }

    public final String resolveBaseName(final Class<?> aClass) {
        return FXUtil.resolveBaseName(aClass);
    }

    public final String resolveBaseName(final Class<?> aClass, final String baseName) {
        return FXUtil.resolveBaseName(aClass, baseName);
    }

    public final void showDocument(final String uri) {
        getHostServices().showDocument(uri);
    }

    public final FXContextBuilder toBuilder() {
        return new FXContextBuilder()
                .setRegistry(getRegistry())
                .setBaseClass(getBaseClass())
                .setCharset(getCharset())
                .setLocale(getLocale())
                .setSkin(getSkin());
    }

    abstract FXRegistry getRegistry();

    final <D, I extends D> void register(final String name,
                                         final Class<D> declarationClass, final Class<I> implementationClass) {
        getRegistry().register(name, declarationClass, implementationClass);
    }

    final <D, I extends D> void register(final String name,
                                         final Class<D> declarationClass, final Class<? extends I> implementationClass,
                                         final I existingBean) {
        getRegistry().register(name, declarationClass, implementationClass, existingBean);
    }

    final <T> void register(final Class<T> type, final T existingBean) {
        register(type.getName(), type, type, existingBean);
    }

    final <T> T registerAndGet(final Class<T> type) {
        register(type.getName(), type, type);
        return getBean(type);
    }
}
