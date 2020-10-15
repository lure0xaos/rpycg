package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelTemplate;
import gargoyle.rpycg.service.CodeConverter;
import gargoyle.rpycg.service.ScriptConverter;
import gargoyle.rpycg.service.Storage;
import gargoyle.rpycg.util.Check;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static gargoyle.rpycg.ex.AppUserException.LC_ERROR_NO_RESOURCES;

public final class Main extends BorderPane implements Initializable {
    private static final String EXTENSION = "rpycg";
    private static final String INSTALL_NAME = "RenPyCheat.rpy";
    private static final String KEY_DEBUG = "debug";
    private static final String LC_CLEAR_CONFIRM = "clear-confirm";
    private static final String LC_ERROR_GENERATE = "error.generate";
    private static final String LC_ERROR_LOAD = "error.load";
    private static final String LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script";
    private static final String LC_ERROR_NOT_GAME = "error.not-game";
    private static final String LC_ERROR_SAVE = "error.save";
    private static final String LC_ERROR_WRITE = "error.write";
    private static final String LC_EXTENSION_DESCRIPTION = "extension-description";
    private static final String LC_LOAD_CONFIRM = "load-confirm";
    private static final String LC_RELOAD_CONFIRM = "reload-confirm";
    private static final String LC_SAVE_AS_CONFIRM = "save-as-confirm";
    private static final String LC_SAVE_CONFIRM = "save-confirm";
    private static final String LC_SUCCESS_GENERATE = "success-generate";
    private static final String LC_SUCCESS_INSTALL = "success-install";
    private static final String LC_TEMPLATE_CONFIRM = "template-confirm";
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnReload;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSaveAs;
    @FXML
    private Builder builder;
    private CodeConverter codeConverter;
    @FXML
    private Creator creator;
    private FolderChooser gameChooser;
    private ResourceBundle resources;
    private ScriptConverter scriptConverter;
    private Storage storage;
    private FileChooser storageChooser;
    @FXML
    private Tab tabBuilder;
    @FXML
    private Tab tabCreator;
    @FXML
    private TabSettings tabSettings;

    public Main() {
        FXLoad.loadComponent(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass()), this, this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        this.resources = Check.requireNonNull(resources, LC_ERROR_NO_RESOURCES, location.toExternalForm());
        scriptConverter = new ScriptConverter();
        codeConverter = new CodeConverter(FXContextFactory.currentContext(), tabSettings.getSettings(),
                CodeConverter.SPACES);
        gameChooser = createGameChooser(tabSettings.getGameDirectory());
        initializeTabs();
        storage = createStorage();
        storageChooser = createStorageChooser(storage.getPath());
        Platform.runLater(() -> RPyCGApp.requestPrevent(getStage(), stage -> doSaveOnClose(resources, stage)));
    }

    @NotNull
    private static FolderChooser createGameChooser(@NotNull Path gameDirectory) {
        FolderChooser directoryChooser = new FolderChooser();
        directoryChooser.setInitialDirectory(gameDirectory);
        return directoryChooser;
    }

    private void initializeTabs() {
        tabCreator.selectedProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                updateScript(true);
                Platform.runLater(() -> creator.onShow());
            }
        });
        tabBuilder.selectedProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                try {
                    updateTreeFromScript();
                    creator.decorateError(Collections.emptySet());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    creator.decorateError(Collections.singleton(e.getLocalizedMessage()));
                    FXDialogs.error(FXContextFactory.currentContext(), getStage(),
                            resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n" + e.getLocalizedMessage());
                }
            }
        });
        builder.changedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                updateScript(false);
                storage.setModified(true);
            }
            builder.setChanged(false);
        });
        creator.changedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                try {
                    updateTreeFromScript();
                    creator.decorateError(Collections.emptySet());
                    storage.setModified(true);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    creator.decorateError(Collections.singleton(e.getLocalizedMessage()));
                }
            }
            creator.setChanged(false);
        });
    }

    @NotNull
    private Storage createStorage() {
        Storage newStorage = new Storage();
        BooleanBinding nullBinding = Bindings.isNull((ObservableObjectValue<?>) newStorage.pathProperty());
        btnReload.disableProperty().bind(nullBinding);
        btnSave.disableProperty().bind(nullBinding);
        return newStorage;
    }

    @NotNull
    private FileChooser createStorageChooser(@Nullable Path storagePath) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                resources.getString(LC_EXTENSION_DESCRIPTION), "*." + EXTENSION);
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setSelectedExtensionFilter(filter);
        Optional.ofNullable(storagePath).ifPresent(path -> {
            fileChooser.setInitialDirectory(path.getParent().toFile());
            fileChooser.setInitialFileName(path.getFileName().toString());
        });
        return fileChooser;
    }

    @NotNull
    private Stage getStage() {
        return (Stage) btnLoad.getScene().getWindow();
    }

    private void doLoad(@NotNull Path path) {
        storage.setPath(path);
        updateTree(storage.load(path));
        updateScript(true);
        storage.setModified(false);
    }

    private void updateScript(boolean forced) {
        List<String> script = scriptConverter.toScript(builder.getModel());
        if (forced) {
            creator.setScript(script);
        } else {
            creator.setScriptUnforced(script);
        }
    }

    private void updateTreeFromScript() {
        builder.setModel(scriptConverter.fromScript(creator.getScript()));
    }

    private boolean doSave(Stage stage) {
        Path storagePath = Objects.requireNonNull(storage.getPath());
        if (!Files.exists(storagePath) && !FXDialogs.confirm(FXContextFactory.currentContext(), stage,
                resources.getString(LC_SAVE_CONFIRM))) {
            return false;
        }
        return save(stage, storagePath);
    }

    private boolean doSaveAs(Stage stage) {
        File saveFile = storageChooser.showSaveDialog(stage);
        if (saveFile == null) {
            return false;
        }
        Path path = saveFile.toPath();
        if (!Files.exists(path) || FXDialogs.confirm(FXContextFactory.currentContext(), stage,
                resources.getString(LC_SAVE_AS_CONFIRM))) {
            save(stage, path);
        }
        return true;
    }

    private boolean save(Stage stage, Path storagePath) {
        try {
            storage.saveAs(storagePath, builder.getModel());
            storage.setPath(storagePath);
            return true;
        } catch (AppException e) {
            FXContext context = FXContextFactory.currentContext();
            FXDialogs.error(context, stage, resources.getString(LC_ERROR_SAVE), e);
            return false;
        }
    }

    private void updateTree(@NotNull ModelItem root) {
        builder.setModel(root);
    }

    private RPyCGApp.CloseAction doSaveOnClose(@NotNull ResourceBundle resources, @NotNull Stage stage) {
        FXContext context = FXContextFactory.currentContext();
        if (!storage.getModified() ||
                builder.isTreeEmpty() ||
                FXDialogs.confirm(context, stage, resources.getString(LC_SAVE_CONFIRM)))
            return RPyCGApp.CloseAction.CLOSE;
        try {
            Path storagePath = storage.getPath();
            if (storagePath != null) {
                return doSave(stage) ? RPyCGApp.CloseAction.CLOSE : RPyCGApp.CloseAction.KEEP;
            } else {
                return doSaveAs(stage) ? RPyCGApp.CloseAction.CLOSE : RPyCGApp.CloseAction.KEEP;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            FXDialogs.error(context, stage, resources.getString(LC_ERROR_MALFORMED_SCRIPT), e);
            return RPyCGApp.CloseAction.KEEP;
        }
    }

    private void doTemplate() {
        Optional.ofNullable(FXContextFactory.currentContext().getParameters())
                .map(Application.Parameters::getNamed)
                .map(map -> map.get(KEY_DEBUG))
                .ifPresentOrElse((s) -> updateTree(ModelTemplate.getTestTemplateTree()),
                        () -> updateTree(ModelTemplate.getTemplateTree()));
    }

    @FXML
    void onClear(@NotNull ActionEvent actionEvent) {
        if (builder.isTreeEmpty() ||
                FXDialogs.confirm(FXContextFactory.currentContext(), getStage(),
                        resources.getString(LC_CLEAR_CONFIRM))) {
            doClear();
        }
    }

    private void doClear() {
        builder.clearAll();
        updateScript(true);
    }

    @FXML
    void onGenerate(@NotNull ActionEvent actionEvent) {
        Stage stage = getStage();
        FXContext context = FXContextFactory.currentContext();
        try {
            putClipboard(generateCodeString());
            FXDialogs.alert(context, stage, resources.getString(LC_SUCCESS_GENERATE));
        } catch (RuntimeException e) {
            FXDialogs.error(context, stage, resources.getString(LC_ERROR_GENERATE), e);
        }
    }

    private static void putClipboard(@NotNull String content) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);
    }

    @NotNull
    private String generateCodeString() {
        return String.join(System.lineSeparator(), generateCode());
    }

    @NotNull
    private List<String> generateCode() {
        return codeConverter.toCode(builder.getModel());
    }

    @FXML
    void onInstall(@NotNull ActionEvent actionEvent) {
        chooseGameDirectory().ifPresent(gamePath -> {
            Stage stage = getStage();
            FXContext context = FXContextFactory.currentContext();
            if (isGameDirectory(gamePath)) {
                try {
                    Files.writeString(gamePath.resolve("game").resolve(INSTALL_NAME), generateCodeString());
                    FXDialogs.alert(context, stage, resources.getString(LC_SUCCESS_INSTALL));
                    storeGamePath(gamePath);
                } catch (IOException e) {
                    FXDialogs.error(context, stage, resources.getString(LC_ERROR_WRITE), e);
                }
            } else {
                FXDialogs.error(context, stage, resources.getString(LC_ERROR_NOT_GAME));
            }
        });
    }

    @FXML
    void onLoad(@NotNull ActionEvent actionEvent) {
        Stage stage = getStage();
        Optional.ofNullable(storageChooser.showOpenDialog(stage)).map(File::toPath).ifPresent(path -> {
            FXContext context = FXContextFactory.currentContext();
            if (builder.isTreeEmpty() ||
                    FXDialogs.confirm(context, stage, resources.getString(LC_LOAD_CONFIRM))) {
                try {
                    doLoad(path);
                } catch (RuntimeException e) {
                    FXDialogs.error(context, stage, resources.getString(LC_ERROR_LOAD), e);
                }
            }
        });
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    @NotNull
    private Optional<Path> chooseGameDirectory() {
        Stage stage = getStage();
        if (gameChooser.getOwner() == null) {
            gameChooser.initOwner(stage);
        }
        Optional.ofNullable(gameChooser.getInitialDirectory()).ifPresent(directory -> {
            Path initialDirectory = directory;
            while (!Files.exists(initialDirectory)) {
                initialDirectory = initialDirectory.getParent();
                gameChooser.setInitialDirectory(initialDirectory);
            }
        });
        return Optional.ofNullable(gameChooser.showDialog(stage));
    }

    private static boolean isGameDirectory(@Nullable Path path) {
        return path != null &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"));
    }

    @FXML
    void onMenu(@NotNull ActionEvent actionEvent) {
        builder.addRootMenu();
    }

    @FXML
    void onReload(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            FXContext context = FXContextFactory.currentContext();
            Stage stage = getStage();
            if (builder.isTreeEmpty() ||
                    FXDialogs.confirm(context, stage, resources.getString(LC_RELOAD_CONFIRM))) {
                try {
                    doLoad(path);
                } catch (RuntimeException e) {
                    FXDialogs.error(context, stage,
                            resources.getString(LC_ERROR_LOAD), e);
                }
            }
        });
    }

    @FXML
    void onSave(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> doSave(getStage()));
    }

    @FXML
    void onSaveAs(@NotNull ActionEvent actionEvent) {
        doSaveAs(getStage());
    }

    @FXML
    void onTemplate(@NotNull ActionEvent actionEvent) {
        FXContext context = FXContextFactory.currentContext();
        Stage stage = getStage();
        if (builder.isTreeEmpty() ||
                FXDialogs.confirm(context, stage, resources.getString(LC_TEMPLATE_CONFIRM))) {
            try {
                doTemplate();
            } catch (IllegalArgumentException | IllegalStateException e) {
                FXDialogs.error(context, stage, resources.getString(LC_ERROR_MALFORMED_SCRIPT), e);
            }
            updateScript(true);
        }
    }

    private void storeGamePath(@NotNull Path gamePath) {
        tabSettings.setGameDirectory((gamePath));
        gameChooser.setInitialDirectory(gamePath);
    }

    @FXML
    void onVariable(@NotNull ActionEvent actionEvent) {
        builder.addRootVariable();
    }
}
