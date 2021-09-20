package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.model.ModelItem;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Storage {
    public static final String LC_ERROR_LOAD = "error.load";
    public static final String LC_ERROR_SAVE = "error.save";
    private final ScriptConverter converter;
    private final Property<Path> gamePath;
    private final Property<Boolean> modified;
    private final Property<Path> path;

    public Storage() {
        converter = new ScriptConverter();
        path = new SimpleObjectProperty<>();
        gamePath = new SimpleObjectProperty<>();
        modified = new SimpleBooleanProperty(false);
    }

    public Property<Path> gamePathProperty() {
        return gamePath;
    }

    public Path getGamePath() {
        return gamePath.getValue();
    }

    public void setGamePath(final Path path) {
        this.gamePath.setValue(path);
    }

    public Boolean getModified() {
        return modified.getValue();
    }

    public void setModified(final Boolean modified) {
        this.modified.setValue(modified);
    }

    public Path getPath() {
        return path.getValue();
    }

    public void setPath(final Path path) {
        this.path.setValue(path);
    }

    public ModelItem load(final Path loadPath) {
        path.setValue(loadPath);
        return reload();
    }

    public Property<Boolean> modifiedProperty() {
        return modified;
    }

    public Property<Path> pathProperty() {
        return path;
    }

    public void saveAs(final Path savePath, final ModelItem root) {
        path.setValue(savePath);
        save(root);
    }

    private ModelItem reload() {
        try {
            final ModelItem item = converter.fromScript(Files.readAllLines(path.getValue()));
            modified.setValue(false);
            return item;
        } catch (final IOException e) {
            modified.setValue(true);
            throw new AppUserException(e, LC_ERROR_LOAD, path.toString());
        }
    }

    private void save(final ModelItem root) {
        try {
            Files.writeString(path.getValue(), String.join(System.lineSeparator(), converter.toScript(root)));
            modified.setValue(false);
        } catch (final IOException e) {
            throw new AppUserException(e, LC_ERROR_SAVE, path.toString());
        }
    }
}
