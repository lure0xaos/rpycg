package gargoyle.rpycg.fx;

import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public final class FXComponent<C, V extends Parent> {
    private final String baseName;
    private final FXContext context;
    private final C controller;
    private final URL location;
    private final V view;

    FXComponent(final FXContext context, final URL location, final String baseName, final C controller, final V view) {
        this.context = context;
        this.location = location;
        this.baseName = baseName;
        this.controller = controller;
        this.view = view;
    }

    public Optional<URL> findResource(final String resourceName, final String suffix) {
        return FXIntUtil.findResource(context.getBaseClass(), context.getLocale(),
                context.resolveBaseName(this.baseName, resourceName), suffix);
    }

    public Optional<URL> findResource(final String suffix) {
        return FXIntUtil.findResource(context.getBaseClass(), context.getLocale(), baseName, suffix);
    }

    public String getBaseName() {
        return baseName;
    }

    public C getController() {
        return controller;
    }

    public URL getLocation() {
        return location;
    }

    public Stage getPrimaryStage() {
        return context.getStage();
    }

    public Stage getStage() {
        return FXUtil.findStage(view).orElseThrow(() -> new FXException("no Stage"));
    }

    public V getView() {
        return view;
    }

    public <R, V2 extends Parent> Optional<FXComponent<Dialog<R>, V2>> loadDialog(final Dialog<R> dialog) {
        return FXIntUtil.loadDialog(context, dialog);
    }

    public Optional<ResourceBundle> loadResources(final Class<?> aClass) {
        return FXIntUtil.loadResources(context.getBaseClass(), context.getLocale(), FXUtil.resolveBaseName(aClass));
    }

    public Optional<ResourceBundle> loadResources(final String bundleName) {
        return FXIntUtil.loadResources(context.getBaseClass(), context.getLocale(), bundleName);
    }

    public Optional<ResourceBundle> loadResources() {
        return FXIntUtil.loadResources(context.getBaseClass(), context.getLocale(), baseName);
    }

    public String resolveBaseName(final String name) {
        return FXUtil.resolveBaseName(baseName, name);
    }

    public String resolveBaseName(final Class<?> aClass) {
        return FXUtil.resolveBaseName(aClass);
    }

    public String resolveBaseName(final Class<?> aClass, final String resourceName) {
        return FXUtil.resolveBaseName(aClass, resourceName);
    }

    @Override
    public String toString() {
        return MessageFormat.format("FXComponent'{'baseName=''{0}'', location={1}'}'", baseName, location);
    }
}
