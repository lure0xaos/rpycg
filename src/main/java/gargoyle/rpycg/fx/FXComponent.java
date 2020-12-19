package gargoyle.rpycg.fx;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public final class FXComponent<C, V> {
    private final Property<String> baseName;
    private final Property<FXContext> context;
    private final Property<C> controller;
    private final Property<URL> location;
    private final Property<V> view;

    FXComponent(FXContext context, URL location, String baseName, C controller, V view) {
        this.context = new SimpleObjectProperty<>(FXContextFactory.snapshot(context));
        this.location = new SimpleObjectProperty<>(location);
        this.baseName = new SimpleStringProperty(baseName);
        this.controller = new SimpleObjectProperty<>(controller);
        this.view = new SimpleObjectProperty<>(view);
    }

    public ReadOnlyProperty<String> baseNameProperty() {
        return baseName;
    }

    public ReadOnlyProperty<FXContext> contextProperty() {
        return context;
    }

    public ReadOnlyProperty<C> controllerProperty() {
        return controller;
    }

    public Optional<URL> findResource(String baseName, String suffix) {
        return getContext().findResource(getContext().getBaseName(this.baseName.getValue(), baseName), suffix);
    }

    public FXContext getContext() {
        return context.getValue();
    }

    public Optional<URL> findResource(String suffix) {
        return getContext().findResource(this, suffix);
    }

    public String getBaseName() {
        return baseName.getValue();
    }

    public C getController() {
        return controller.getValue();
    }

    public URL getLocation() {
        return location.getValue();
    }

    public V getView() {
        return view.getValue();
    }

    public <R, V2 extends Parent> Optional<FXComponent<Dialog<R>, V2>> loadDialog(Dialog<R> dialog) {
        return getContext().loadDialog(dialog);
    }

    public Optional<ResourceBundle> loadResources() {
        return getContext().loadResources(baseName.getValue());
    }

    public ReadOnlyProperty<URL> locationProperty() {
        return location;
    }

    public ReadOnlyProperty<V> viewProperty() {
        return view;
    }
}
