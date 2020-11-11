package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.model.ModelItem;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Storage {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.Storage")
    public static final String LC_ERROR_LOAD = "error.load";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.service.Storage")
    public static final String LC_ERROR_SAVE = "error.save";
    @NotNull
    private final ScriptConverter converter;
    @NotNull
    private final Property<Boolean> modified;
    @NotNull
    private final Property<Path> path;

    public Storage() {
        converter = new ScriptConverter();
        path = new SimpleObjectProperty<>();
        modified = new SimpleBooleanProperty(false);
    }

    public Boolean getModified() {
        return modified.getValue();
    }

    public void setModified(Boolean modified) {
        this.modified.setValue(modified);
    }

    @Nullable
    public Path getPath() {
        return path.getValue();
    }

    public void setPath(@NotNull Path path) {
        this.path.setValue(path);
    }

    @NotNull
    public ModelItem load(@NotNull Path loadPath) {
        path.setValue(loadPath);
        return reload();
    }

    @NotNull
    private ModelItem reload() {
        try {
            ModelItem item = converter.fromScript(Files.readAllLines(path.getValue()));
            modified.setValue(false);
            return item;
        } catch (IOException e) {
            modified.setValue(true);
            throw new AppUserException(e, LC_ERROR_LOAD, path.toString());
        }
    }

    public @NotNull Property<Boolean> modifiedProperty() {
        return modified;
    }

    @NotNull
    public Property<Path> pathProperty() {
        return path;
    }

    public void saveAs(@NotNull Path savePath, @NotNull ModelItem root) {
        path.setValue(savePath);
        save(root);
    }

    private void save(@NotNull ModelItem root) {
        try {
            Files.writeString(path.getValue(), String.join(System.lineSeparator(), converter.toScript(root)));
            modified.setValue(false);
        } catch (IOException e) {
            throw new AppUserException(e, LC_ERROR_SAVE, path.toString());
        }
    }
}
