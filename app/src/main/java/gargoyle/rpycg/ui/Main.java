package gargoyle.rpycg.ui;

import gargoyle.fx.FXCloseAction;
import gargoyle.fx.FXComponent;
import gargoyle.fx.FXContext;
import gargoyle.fx.FXContextFactory;
import gargoyle.fx.FXLauncher;
import gargoyle.fx.FXRun;
import gargoyle.fx.FXUtil;
import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ex.CodeGenerationException;
import gargoyle.rpycg.ex.MalformedScriptException;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.service.CodeConverter;
import gargoyle.rpycg.service.ErrorMailer;
import gargoyle.rpycg.service.ScriptConverter;
import gargoyle.rpycg.service.Storage;
import gargoyle.rpycg.ui.icons.Icon;
import gargoyle.rpycg.util.GameUtil;
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

import java.io.Closeable;
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

public final class Main extends BorderPane implements Initializable, Closeable {
    private static final String EXTENSION = "rpycg";
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
    private final FXComponent<Main, Main> component;
    @FXML
    private MenuItem btnLoad;
    @FXML
    private MenuButton btnLoadReload;
    @FXML
    private MenuItem btnReinstall;
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
        component = FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException("No view {resource}", Map.of("resource", Main.class.getName())));
    }

    private static FolderChooser createGameChooser(final ResourceBundle resources, final Path gameDirectory) {
        final FolderChooser directoryChooser = new FolderChooser();
        Optional.ofNullable(resources).ifPresent(bundle ->
                directoryChooser.setTitle(bundle.getString(LC_GAME_CHOOSER_TITLE)));
        directoryChooser.setInitialDirectory(gameDirectory);
        directoryChooser.setSelectionFilter(GameUtil::isGameDirectory);
        directoryChooser.setAdditionalIconProvider((path, expanded) -> {
            if (GameUtil.isGameDirectory(path)) {
                return (expanded ? Icon.GAME_FOLDER_OPEN : Icon.GAME_FOLDER)
                        .findIcon(FXContextFactory.currentContext())
                        .map(URL::toExternalForm)
                        .map(ImageView::new);
            }
            return Optional.empty();
        });
        return directoryChooser;
    }

    private static void putClipboard(final String content) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);
    }

    public void close() {
        if (null != gameChooser) {
            gameChooser.dispose();
        }
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.resources = FXUtil.requireNonNull(resources, "No resources {location}",
                Map.of("location", location.toExternalForm()));
        scriptConverter = new ScriptConverter();
        codeConverter = new CodeConverter(FXContextFactory.currentContext(), tabSettings.getSettings());
        gameChooser = createGameChooser(resources, tabSettings.getGameDirectory());
        initializeTabs();
        storage = createStorage();
        storageChooser = createStorageChooser(storage.getPath(), tabSettings.getStorageDirectory());
        FXRun.runLater(() -> FXLauncher.requestPrevent(FXContextFactory.currentContext(),
                stage -> doSaveOnClose(resources)));
    }

    @FXML
    void onClear(final ActionEvent actionEvent) {
        if (builder.isTreeEmpty() ||
                FXContextFactory.currentContext().confirm(resources.getString(LC_CLEAR_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLEAR_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CLEAR_CONFIRM_CANCEL)))) {
            doClear();
        }
    }

    @FXML
    void onGenerate(final ActionEvent actionEvent) {
        final FXContext context = FXContextFactory.currentContext();
        try {
            putClipboard(generateCodeString());
            context.alert(resources.getString(LC_SUCCESS_GENERATE))
                    .ifPresent(buttonType -> sceneProperty());
        } catch (final CodeGenerationException e) {
            context.error(resources.getString(LC_ERROR_GENERATE) + "\n" + e.getLocalizedMessage());
        } catch (final RuntimeException e) {
            context.error(resources.getString(LC_ERROR_GENERATE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
        }
    }

    @FXML
    void onInstall(final ActionEvent actionEvent) {
        chooseGameDirectory().ifPresent(this::doInstall);
    }

    @FXML
    void onLoad(final ActionEvent actionEvent) {
        Optional.ofNullable(storageChooser.showOpenDialog(component.getStage())).map(File::toPath)
                .ifPresent(path -> {
                    final FXContext context = FXContextFactory.currentContext();
                    if (builder.isTreeEmpty() || context.confirm(resources.getString(LC_LOAD_CONFIRM), Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_LOAD_CONFIRM_OK),
                            ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_LOAD_CONFIRM_CANCEL)))) {
                        try {
                            doLoad(path);
                        } catch (final MalformedScriptException e) {
                            context.error(resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n"
                                    + e.getLocalizedMessage());
                        } catch (final RuntimeException e) {
                            context.error(resources.getString(LC_ERROR_LOAD), e, Map.of(
                                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
                        }
                    }
                });
    }

    @FXML
    void onMenu(final ActionEvent actionEvent) {
        builder.addRootMenu();
    }

    @FXML
    void onReinstall(final ActionEvent actionEvent) {
        Optional.of(storage.getGamePath()).ifPresent(this::doInstall);
    }

    @FXML
    void onReload(final ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            final FXContext context = FXContextFactory.currentContext();
            if (builder.isTreeEmpty() ||
                    context.confirm(resources.getString(LC_RELOAD_CONFIRM), Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_RELOAD_CONFIRM_OK),
                            ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_RELOAD_CONFIRM_CANCEL)))) {
                try {
                    doLoad(path);
                } catch (final MalformedScriptException e) {
                    context.error(resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n" + e.getLocalizedMessage());
                } catch (final RuntimeException e) {
                    context.error(resources.getString(LC_ERROR_LOAD), e, Map.of(
                                    ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                    ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                            .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                            .ifPresent(buttonType -> ErrorMailer.mailError(e));
                }
            }
        });
    }

    @FXML
    void onSave(final ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            try {
                doSave();
            } catch (final RuntimeException e) {
                FXContextFactory.currentContext().error(resources.getString(LC_ERROR_SAVE), e, Map.of(
                                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                        .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                        .ifPresent(buttonType -> ErrorMailer.mailError(e));
            }
        });
    }

    @FXML
    void onSaveAs(final ActionEvent actionEvent) {
        try {
            doSaveAs();
        } catch (final RuntimeException e) {
            FXContextFactory.currentContext().error(resources.getString(LC_ERROR_SAVE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
        }
    }

    @FXML
    void onVariable(final ActionEvent actionEvent) {
        builder.addRootVariable();
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    private Optional<Path> chooseGameDirectory() {
        if (null == gameChooser.getOwner()) {
            gameChooser.initOwner(component.getStage());
        }
        Optional.ofNullable(gameChooser.getInitialDirectory()).ifPresent(directory -> {
            Path initialDirectory = directory;
            while (!Files.exists(initialDirectory)) {
                initialDirectory = initialDirectory.getParent();
                gameChooser.setInitialDirectory(initialDirectory);
            }
        });
        return Optional.ofNullable(gameChooser.showDialog());
    }

    private Storage createStorage() {
        final Storage newStorage = new Storage();
        {
            final BooleanBinding nullBinding = Bindings.isNull((ObservableObjectValue<?>) newStorage.pathProperty());
            btnReload.disableProperty().bind(nullBinding);
            btnSave.disableProperty().bind(nullBinding);
        }
        {
            final BooleanBinding nullBinding = Bindings.isNull((ObservableObjectValue<?>) newStorage.gamePathProperty());
            btnReinstall.disableProperty().bind(nullBinding);
        }
        return newStorage;
    }

    private FileChooser createStorageChooser(final Path storagePath, final Path storageDirectory) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
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

    private void doClear() {
        builder.clearAll();
        updateScript(true);
    }

    private void doInstall(final Path gamePath) {
        final FXContext context = FXContextFactory.currentContext();
        if (!GameUtil.isGameDirectory(gamePath)) {
            context.error(resources.getString(LC_ERROR_NOT_GAME));
            return;
        }
        try {
            Files.writeString(gamePath.resolve("game").resolve(INSTALL_NAME), generateCodeString());
            context.alert(resources.getString(LC_SUCCESS_INSTALL));
            storeGamePath(gamePath);
        } catch (final IOException | CodeGenerationException e) {
            context.error(resources.getString(LC_ERROR_WRITE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
        }
    }

    private void doLoad(final Path path) {
        storage.setPath(path);
        tabSettings.setStorageDirectory(path.getParent());
        updateTree(storage.load(path));
        updateScript(true);
        storage.setModified(false);
    }

    private boolean doSave() {
        final Path storagePath = Objects.requireNonNull(storage.getPath());
        if (Files.exists(storagePath) || FXContextFactory.currentContext().confirm(
                resources.getString(LC_SAVE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_SAVE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_SAVE_CONFIRM_CANCEL)))) {
            return save(storagePath);
        }
        return false;
    }

    private boolean doSaveAs() {
        final File saveFile = storageChooser.showSaveDialog(component.getStage());
        if (null == saveFile) {
            return false;
        }
        final Path path = saveFile.toPath();
        if (!Files.exists(path) || FXContextFactory.currentContext().confirm(resources.getString(LC_SAVE_AS_CONFIRM),
                Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_SAVE_AS_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_SAVE_AS_CONFIRM_CANCEL)
                ))) {
            save(path);
            tabSettings.setStorageDirectory(path.getParent());
        }
        return true;
    }

    private FXCloseAction doSaveOnClose(final ResourceBundle resources) {
        final FXContext context = FXContextFactory.currentContext();
        if (!storage.getModified() || builder.isTreeEmpty() ||
                context.confirm(resources.getString(LC_CLOSE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CLOSE_CONFIRM_CANCEL)
                )))
            return FXCloseAction.CLOSE;
        try {
            final Path storagePath = storage.getPath();
            if (null != storagePath) {
                return doSave() ? FXCloseAction.CLOSE : FXCloseAction.KEEP;
            } else {
                return doSaveAs() ? FXCloseAction.CLOSE : FXCloseAction.KEEP;
            }
        } catch (final IllegalArgumentException | IllegalStateException e) {
            context.error(resources.getString(LC_ERROR_MALFORMED_SCRIPT), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
            return FXCloseAction.KEEP;
        }
    }

    private List<String> generateCode() {
        return codeConverter.toCode(builder.getModel());
    }

    private String generateCodeString() {
        return String.join(System.lineSeparator(), generateCode());
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
                } catch (final IllegalArgumentException | IllegalStateException | MalformedScriptException e) {
                    creator.decorateError(Collections.singleton(e.getLocalizedMessage()));
                    FXContextFactory.currentContext().error(resources.getString(LC_ERROR_MALFORMED_SCRIPT) + "\n"
                            + e.getLocalizedMessage());
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
                } catch (final IllegalArgumentException | IllegalStateException | MalformedScriptException e) {
                    creator.decorateError(Collections.singleton(e.getLocalizedMessage()));
                }
            }
            creator.setChanged(false);
        });
    }

    private boolean save(final Path storagePath) {
        try {
            storage.saveAs(storagePath, builder.getModel());
            storage.setPath(storagePath);
            return true;
        } catch (final AppException e) {
            FXContextFactory.currentContext().error(resources.getString(LC_ERROR_SAVE), e, Map.of(
                            ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                            ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                    .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                    .ifPresent(buttonType -> ErrorMailer.mailError(e));
            return false;
        }
    }

    private void storeGamePath(final Path gamePath) {
        tabSettings.setGameDirectory((gamePath));
        gameChooser.setInitialDirectory(gamePath);
        storage.setGamePath(gamePath);
    }

    private void updateScript(final boolean forced) {
        final List<String> script = scriptConverter.toScript(builder.getModel());
        if (forced) {
            creator.setScript(script);
        } else {
            creator.setScriptUnforced(script);
        }
    }

    private void updateTree(final ModelItem root) {
        builder.setModel(root);
    }

    private void updateTreeFromScript() {
        builder.setModel(scriptConverter.fromScript(creator.getScript()));
    }
}
