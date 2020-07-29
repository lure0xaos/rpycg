package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.model.ModelItem;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Storage {
    @NotNull
    private final ScriptConverter converter;

    @NotNull
    private final Property<Path> path;

    public Storage() {
        converter = new ScriptConverter();
        path = new SimpleObjectProperty<>();
    }

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
    public ModelItem reload() {
        try {
            return converter.fromScript(Files.readAllLines(path.getValue()));
        } catch (IOException e) {
            throw new AppException("cannot load from " + path, e);
        }
    }

    @NotNull
    public Property<Path> pathProperty() {
        return path;
    }

    public void saveAs(@NotNull Path savePath, @NotNull ModelItem root) {
        path.setValue(savePath);
        save(root);
    }

    public void save(@NotNull ModelItem root) {
        try {
            Files.writeString(path.getValue(), String.join(System.lineSeparator(), converter.toScript(root)));
        } catch (IOException e) {
            throw new AppException("cannot save to " + path, e);
        }
    }
}
