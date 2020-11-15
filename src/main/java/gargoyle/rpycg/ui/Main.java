package gargoyle.rpycg.ui;

import gargoyle.rpycg.RPyCG;
import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ex.CodeGenerationException;
import gargoyle.rpycg.ex.MalformedScriptException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLauncher;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.service.CodeConverter;
import gargoyle.rpycg.service.ScriptConverter;
import gargoyle.rpycg.service.Storage;
import gargoyle.rpycg.util.Check;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public final class Main extends BorderPane implements Initializable {
    private static final String EXTENSION = "rpycg";
    private static final String INSTALL_NAME = "RenPyCheat.rpy";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLEAR_CONFIRM = "clear-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLEAR_CONFIRM_CANCEL = "clear-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLEAR_CONFIRM_OK = "clear-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLOSE = "close";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLOSE_CONFIRM = "close-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLOSE_CONFIRM_CANCEL = "close-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_CLOSE_CONFIRM_OK = "close-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_GENERATE = "error.generate";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_LOAD = "error.load";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_NOT_GAME = "error.not-game";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_SAVE = "error.save";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_ERROR_WRITE = "error.write";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_EXTENSION_DESCRIPTION = "extension-description";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_LOAD_CONFIRM = "load-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_LOAD_CONFIRM_CANCEL = "load-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_LOAD_CONFIRM_OK = "load-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_RELOAD_CONFIRM = "reload-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_RELOAD_CONFIRM_CANCEL = "reload-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_RELOAD_CONFIRM_OK = "reload-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_REPORT = "report";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_AS_CONFIRM = "save-as-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_AS_CONFIRM_CANCEL = "save-as-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_AS_CONFIRM_OK = "save-as-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_CONFIRM = "save-confirm";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_CONFIRM_CANCEL = "save-confirm-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SAVE_CONFIRM_OK = "save-confirm-ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SUCCESS_GENERATE = "success-generate";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.Main")
    private static final String LC_SUCCESS_INSTALL = "success-install";
    @FXML
    private MenuItem btnLoad;
    @FXML
    private MenuButton btnLoadReload;
    @FXML
    private MenuItem btnReload;
    @FXML
    private MenuItem btnSave;
    @FXML
    private MenuItem btnSaveAs;
    @FXML
    private MenuButton btnSaveSaveAs;
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
        FXLoad.loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        this.resources = Check.requireNonNull(resources, AppUserException.LC_ERROR_NO_RESOURCES, location.toExternalForm());
        scriptConverter = new ScriptConverter();
        codeConverter = new CodeConverter(FXContextFactory.currentContext(), tabSettings.getSettings(),
                CodeConverter.SPACES);
        gameChooser = createGameChooser(tabSettings.getGameDirectory());
        initializeTabs();
        storage = createStorage();
        storageChooser = createStorageChooser(storage.getPath());
        FXRun.runLater(() -> FXLauncher.requestPrevent(getStage().orElseThrow(), stage -> doSaveOnClose(resources, stage)));
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
                FXRun.runLater(() -> creator.onShow());
            }
        });
        tabBuilder.selectedProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                try {
                    updateTreeFromScript();
                    creator.decorateError(Collections.emptySet());
                } catch (IllegalArgumentException | IllegalStateException | MalformedScriptException e) {
                    creator.decorateError(Collections.singleton(e.getLocalizedMessage()));
                    FXDialogs.error(getStage().orElse(null),
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
                } catch (IllegalArgumentException | IllegalStateException | MalformedScriptException e) {
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
    private Optional<Stage> getStage() {
        return FXUtil.findStage(btnLoadReload);
    }

    private FXLauncher.FXCloseAction doSaveOnClose(@NotNull ResourceBundle resources, @NotNull Stage stage) {
        if (!storage.getModified() || builder.isTreeEmpty() ||
                FXDialogs.confirm(stage, resources.getString(LC_CLOSE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CLOSE_CONFIRM_CANCEL)
                )))
            return FXLauncher.FXCloseAction.CLOSE;
        try {
            Path storagePath = storage.getPath();
            if (storagePath != null) {
                return doSave(stage) ? FXLauncher.FXCloseAction.CLOSE : FXLauncher.FXCloseAction.KEEP;
            } else {
                return doSaveAs(stage) ? FXLauncher.FXCloseAction.CLOSE : FXLauncher.FXCloseAction.KEEP;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            FXDialogs.error(stage, resources.getString(LC_ERROR_MALFORMED_SCRIPT), e, Map.of(
                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                    .ifPresent(buttonType -> RPyCG.mailError(e));
            return FXLauncher.FXCloseAction.KEEP;
        }
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
        if (Files.exists(storagePath) || FXDialogs.confirm(stage, resources.getString(LC_SAVE_CONFIRM), Map.of(
                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_SAVE_CONFIRM_OK),
                ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_SAVE_CONFIRM_CANCEL)))) {
            return save(stage, storagePath);
        }
        return false;
    }

    private boolean doSaveAs(Stage stage) {
        File saveFile = storageChooser.showSaveDialog(stage);
        if (saveFile == null) {
            return false;
        }
        Path path = saveFile.toPath();
        if (!Files.exists(path) || FXDialogs.confirm(stage, resources.getString(LC_SAVE_AS_CONFIRM), Map.of(
                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_SAVE_AS_CONFIRM_OK),
                ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_SAVE_AS_CONFIRM_CANCEL)
        ))) {
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
            FXDialogs.error(stage, resources.getString(LC_ERROR_SAVE), e, Map.of(
                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                    .ifPresent(buttonType -> RPyCG.mailError(e));
            return false;
        }
    }

    @FXML
    void onClear(@NotNull ActionEvent actionEvent) {
        if (builder.isTreeEmpty() ||
                FXDialogs.confirm(getStage().orElse(null), resources.getString(LC_CLEAR_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLEAR_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CLEAR_CONFIRM_CANCEL)))) {
            doClear();
        }
    }

    private void doClear() {
        builder.clearAll();
        updateScript(true);
    }

    @FXML
    void onGenerate(@NotNull ActionEvent actionEvent) {
        try {
            putClipboard(generateCodeString());
            FXDialogs.alert(getStage().orElse(null), resources.getString(LC_SUCCESS_GENERATE));
        } catch (CodeGenerationException e) {
            FXDialogs.error(getStage().orElse(null),
                    resources.getString(LC_ERROR_GENERATE) + "\n" + e.getLocalizedMessage());
        } catch (RuntimeException e) {
            FXDialogs.error(getStage().orElse(null), resources.getString(LC_ERROR_GENERATE), e, Map.of(
                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                    .ifPresent(buttonType -> RPyCG.mailError(e));
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
            if (isGameDirectory(gamePath)) {
                try {
                    Files.writeString(gamePath.resolve("game").resolve(INSTALL_NAME), generateCodeString());
                    FXDialogs.alert(getStage().orElse(null), resources.getString(LC_SUCCESS_INSTALL));
                    storeGamePath(gamePath);
                } catch (IOException | CodeGenerationException e) {
                    FXDialogs.error(getStage().orElse(null), resources.getString(LC_ERROR_WRITE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                            .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                            .ifPresent(buttonType -> RPyCG.mailError(e));
                }
            } else {
                FXDialogs.error(getStage().orElse(null), resources.getString(LC_ERROR_NOT_GAME));
            }
        });
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    @NotNull
    private Optional<Path> chooseGameDirectory() {
        if (gameChooser.getOwner() == null) {
            gameChooser.initOwner(getStage().orElse(null));
        }
        Optional.ofNullable(gameChooser.getInitialDirectory()).ifPresent(directory -> {
            Path initialDirectory = directory;
            while (!Files.exists(initialDirectory)) {
                initialDirectory = initialDirectory.getParent();
                gameChooser.setInitialDirectory(initialDirectory);
            }
        });
        return Optional.ofNullable(gameChooser.showDialog(getStage().orElseThrow()));
    }

    private static boolean isGameDirectory(@Nullable Path path) {
        return path != null &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"));
    }

    private void storeGamePath(@NotNull Path gamePath) {
        tabSettings.setGameDirectory((gamePath));
        gameChooser.setInitialDirectory(gamePath);
    }

    @FXML
    void onLoad(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storageChooser.showOpenDialog(getStage().orElse(null))).map(File::toPath)
                .ifPresent(path -> {
                    if (builder.isTreeEmpty() || FXDialogs.confirm(getStage().orElse(null),
                            resources.getString(LC_LOAD_CONFIRM), Map.of(
                                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_LOAD_CONFIRM_OK),
                                    ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_LOAD_CONFIRM_CANCEL)))) {
                        try {
                            doLoad(path);
                        } catch (MalformedScriptException e) {
                            FXDialogs.error(getStage().orElse(null),
                                    resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n" + e.getLocalizedMessage());
                        } catch (RuntimeException e) {
                            FXDialogs.error(getStage().orElse(null), resources.getString(LC_ERROR_LOAD), e, Map.of(
                                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                                    .ifPresent(buttonType -> RPyCG.mailError(e));
                        }
                    }
                });
    }

    private void doLoad(@NotNull Path path) {
        storage.setPath(path);
        updateTree(storage.load(path));
        updateScript(true);
        storage.setModified(false);
    }

    private void updateTree(@NotNull ModelItem root) {
        builder.setModel(root);
    }

    @FXML
    void onMenu(@NotNull ActionEvent actionEvent) {
        builder.addRootMenu();
    }

    @FXML
    void onReload(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            if (builder.isTreeEmpty() ||
                    FXDialogs.confirm(getStage().orElse(null), resources.getString(LC_RELOAD_CONFIRM), Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_RELOAD_CONFIRM_OK),
                            ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_RELOAD_CONFIRM_CANCEL)))) {
                try {
                    doLoad(path);
                } catch (MalformedScriptException e) {
                    FXDialogs.error(getStage().orElse(null),
                            resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n" + e.getLocalizedMessage());
                } catch (RuntimeException e) {
                    FXDialogs.error(getStage().orElse(null),
                            resources.getString(LC_ERROR_LOAD), e, Map.of(
                                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                            .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                            .ifPresent(buttonType -> RPyCG.mailError(e));
                }
            }
        });
    }

    @FXML
    void onSave(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            try {
                doSave(getStage().orElse(null));
            } catch (RuntimeException e) {
                FXDialogs.error(getStage().orElse(null),
                        resources.getString(LC_ERROR_SAVE), e, Map.of(
                                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                        .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                        .ifPresent(buttonType -> RPyCG.mailError(e));
            }
        });
    }

    @FXML
    void onSaveAs(@NotNull ActionEvent actionEvent) {
        try {
            doSaveAs(getStage().orElse(null));
        } catch (RuntimeException e) {
            FXDialogs.error(getStage().orElse(null),
                    resources.getString(LC_ERROR_SAVE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                    .ifPresent(buttonType -> RPyCG.mailError(e));
        }
    }

    @FXML
    void onVariable(@NotNull ActionEvent actionEvent) {
        builder.addRootVariable();
    }
}
