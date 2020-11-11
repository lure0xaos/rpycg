package gargoyle.rpycg.fx;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public ReadOnlyProperty<String> baseNameProperty() {
        return baseName;
    }

    @NotNull
    public ReadOnlyProperty<FXContext> contextProperty() {
        return context;
    }

    @NotNull
    public ReadOnlyProperty<C> controllerProperty() {
        return controller;
    }

    @NotNull
    public Optional<URL> findResource(@NotNull String baseName, @NotNull String suffix) {
        return FXLoad.findResource(context.getValue(), FXLoad.getBaseName(this.baseName.getValue(), baseName), suffix);
    }

    @NotNull
    public Optional<URL> findResource(@NotNull String suffix) {
        return FXLoad.findResource(context.getValue(), this, suffix);
    }

    public String getBaseName() {
        return baseName.getValue();
    }

    public FXContext getContext() {
        return context.getValue();
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

    public <R, V2 extends Parent> Optional<FXComponent<Dialog<R>, V2>> loadDialog(@NotNull Dialog<R> dialog) {
        return FXLoad.loadDialog(context.getValue(), dialog);
    }

    @NotNull
    public Optional<ResourceBundle> loadResources() {
        return FXLoad.loadResources(context.getValue(), baseName.getValue());
    }

    @NotNull
    public ReadOnlyProperty<URL> locationProperty() {
        return location;
    }

    @NotNull
    public ReadOnlyProperty<V> viewProperty() {
        return view;
    }
}
