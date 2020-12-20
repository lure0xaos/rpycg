package gargoyle.rpycg.ui;

import gargoyle.rpycg.RPyCG;
import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ex.CodeGenerationException;
import gargoyle.rpycg.ex.MalformedScriptException;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLauncher;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.service.CodeConverter;
import gargoyle.rpycg.service.ScriptConverter;
import gargoyle.rpycg.service.Storage;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    private static final String ICON_GAME_FOLDER = "icons/game-folder";
    private static final String ICON_GAME_FOLDER_OPEN = "icons/game-folder-open";
    private static final String INSTALL_NAME = "RenPyCheat.rpy";
    private static final String LC_CLEAR_CONFIRM = "clear-confirm";
    private static final String LC_CLEAR_CONFIRM_CANCEL = "clear-confirm-cancel";
    private static final String LC_CLEAR_CONFIRM_OK = "clear-confirm-ok";
    private static final String LC_CLOSE = "close";
    private static final String LC_CLOSE_CONFIRM = "close-confirm";
    private static final String LC_CLOSE_CONFIRM_CANCEL = "close-confirm-cancel";
    private static final String LC_CLOSE_CONFIRM_OK = "close-confirm-ok";
    private static final String LC_ERROR_GENERATE = "error.generate";
    private static final String LC_ERROR_LOAD = "error.load";
    private static final String LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script";
    private static final String LC_ERROR_NOT_GAME = "error.not-game";
    private static final String LC_ERROR_SAVE = "error.save";
    private static final String LC_ERROR_WRITE = "error.write";
    private static final String LC_EXTENSION_DESCRIPTION = "extension-description";
    private static final String LC_GAME_CHOOSER_TITLE = "game-chooser-title";
    private static final String LC_LOAD_CONFIRM = "load-confirm";
    private static final String LC_LOAD_CONFIRM_CANCEL = "load-confirm-cancel";
    private static final String LC_LOAD_CONFIRM_OK = "load-confirm-ok";
    private static final String LC_RELOAD_CONFIRM = "reload-confirm";
    private static final String LC_RELOAD_CONFIRM_CANCEL = "reload-confirm-cancel";
    private static final String LC_RELOAD_CONFIRM_OK = "reload-confirm-ok";
    private static final String LC_REPORT = "report";
    private static final String LC_SAVE_AS_CONFIRM = "save-as-confirm";
    private static final String LC_SAVE_AS_CONFIRM_CANCEL = "save-as-confirm-cancel";
    private static final String LC_SAVE_AS_CONFIRM_OK = "save-as-confirm-ok";
    private static final String LC_SAVE_CONFIRM = "save-confirm";
    private static final String LC_SAVE_CONFIRM_CANCEL = "save-confirm-cancel";
    private static final String LC_SAVE_CONFIRM_OK = "save-confirm-ok";
    private static final String LC_SUCCESS_GENERATE = "success-generate";
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
        FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, Main.class.getName()));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES,
                location.toExternalForm());
        scriptConverter = new ScriptConverter();
        codeConverter = new CodeConverter(FXContextFactory.currentContext(), tabSettings.getSettings()
        );
        gameChooser = createGameChooser(resources, tabSettings.getGameDirectory());
        initializeTabs();
        storage = createStorage();
        storageChooser = createStorageChooser(storage.getPath(), tabSettings.getStorageDirectory());
        FXRun.runLater(() -> FXLauncher.requestPrevent(getStage().orElseThrow(),
                stage -> doSaveOnClose(resources, stage)));
    }

    private static FolderChooser createGameChooser(ResourceBundle resources, Path gameDirectory) {
        FolderChooser directoryChooser = new FolderChooser();
        Optional.ofNullable(resources).ifPresent(bundle ->
                directoryChooser.setTitle(bundle.getString(LC_GAME_CHOOSER_TITLE)));
        directoryChooser.setInitialDirectory(gameDirectory);
        directoryChooser.setSelectionFilter(Main::isGameDirectory);
        directoryChooser.setAdditionalIconProvider((path, expanded) -> {
            if (isGameDirectory(path)) {
                FXContext context = FXContextFactory.currentContext();
                return context.findResource(
                        context.getBaseName(Main.class, expanded ? ICON_GAME_FOLDER_OPEN : ICON_GAME_FOLDER),
                        FXConstants.EXT_IMAGES)
                        .map(URL::toExternalForm)
                        .map(ImageView::new);
            }
            return Optional.empty();
        });
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

    private Storage createStorage() {
        Storage newStorage = new Storage();
        BooleanBinding nullBinding = Bindings.isNull((ObservableObjectValue<?>) newStorage.pathProperty());
        btnReload.disableProperty().bind(nullBinding);
        btnSave.disableProperty().bind(nullBinding);
        return newStorage;
    }

    private FileChooser createStorageChooser(Path storagePath, Path storageDirectory) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                resources.getString(LC_EXTENSION_DESCRIPTION), "*." + EXTENSION);
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setSelectedExtensionFilter(filter);
        Optional.ofNullable(storagePath).ifPresentOrElse(path -> {
            fileChooser.setInitialDirectory(path.getParent().toFile());
            fileChooser.setInitialFileName(path.getFileName().toString());
        }, () -> Optional.ofNullable(storageDirectory).ifPresent(path ->
                fileChooser.setInitialDirectory(path.toFile())));
        return fileChooser;
    }

    private Optional<Stage> getStage() {
        return FXUtil.findStage(btnLoadReload);
    }

    private FXLauncher.FXCloseAction doSaveOnClose(ResourceBundle resources, Stage stage) {
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

    private static boolean isGameDirectory(Path path) {
        return path != null &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"));
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
            tabSettings.setStorageDirectory(path.getParent());
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
    void onClear(ActionEvent actionEvent) {
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
    void onGenerate(ActionEvent actionEvent) {
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

    private static void putClipboard(String content) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);
    }

    private String generateCodeString() {
        return String.join(System.lineSeparator(), generateCode());
    }

    private List<String> generateCode() {
        return codeConverter.toCode(builder.getModel());
    }

    @FXML
    void onInstall(ActionEvent actionEvent) {
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

    private void storeGamePath(Path gamePath) {
        tabSettings.setGameDirectory((gamePath));
        gameChooser.setInitialDirectory(gamePath);
    }

    @FXML
    void onLoad(ActionEvent actionEvent) {
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

    private void doLoad(Path path) {
        storage.setPath(path);
        tabSettings.setStorageDirectory(path.getParent());
        updateTree(storage.load(path));
        updateScript(true);
        storage.setModified(false);
    }

    private void updateTree(ModelItem root) {
        builder.setModel(root);
    }

    @FXML
    void onMenu(ActionEvent actionEvent) {
        builder.addRootMenu();
    }

    @FXML
    void onReload(ActionEvent actionEvent) {
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
    void onSave(ActionEvent actionEvent) {
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
    void onSaveAs(ActionEvent actionEvent) {
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
    void onVariable(ActionEvent actionEvent) {
        builder.addRootVariable();
    }
}
