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
    private final Property<Boolean> modified;
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

    public Path getPath() {
        return path.getValue();
    }

    public void setPath(Path path) {
        this.path.setValue(path);
    }

    public ModelItem load(Path loadPath) {
        path.setValue(loadPath);
        return reload();
    }

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

    public Property<Boolean> modifiedProperty() {
        return modified;
    }

    public Property<Path> pathProperty() {
        return path;
    }

    public void saveAs(Path savePath, ModelItem root) {
        path.setValue(savePath);
        save(root);
    }

    private void save(ModelItem root) {
        try {
            Files.writeString(path.getValue(), String.join(System.lineSeparator(), converter.toScript(root)));
            modified.setValue(false);
        } catch (IOException e) {
            throw new AppUserException(e, LC_ERROR_SAVE, path.toString());
        }
    }
}
