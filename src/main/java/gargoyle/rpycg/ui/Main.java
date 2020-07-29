package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.model.ModelTemplate;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.model.Settings;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.service.CodeConverter;
import gargoyle.rpycg.service.ModelConverter;
import gargoyle.rpycg.service.ScriptConverter;
import gargoyle.rpycg.service.Storage;
import gargoyle.rpycg.ui.model.DROPPING;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;

public final class Main extends BorderPane implements Initializable {
    private static final double BOND = 0.3;
    private static final String CLASS_DANGER = "danger";
    private static final String CLASS_EVEN = "even";
    private static final String CLASS_FIRST_CHILD = "first-child";
    private static final String CLASS_LAST_CHILD = "last-child";
    private static final String CLASS_MENU = "menu";
    private static final String CLASS_ODD = "odd";
    private static final String CLASS_VARIABLE = "variable";
    private static final String CLASS_WARN = "warn";
    private static final String COLUMN_LABEL = "labelColumn";
    private static final String COLUMN_MODEL_TYPE = "modelTypeColumn";
    private static final String COLUMN_NAME = "nameColumn";
    private static final String EXTENSION = "rpycg";
    private static final String ICON_EMPTY = "icons/empty.png";
    private static final String ICON_MENU = "icons/menu.png";
    private static final String ICON_VARIABLE = "icons/var.png";
    private static final String INSTALL_NAME = "RenPyCheat.rpy";
    private static final int MENU_SIZE_DANGER = 12;
    private static final int MENU_SIZE_WARN = 10;
    private static final String PREF_GAME = "game";
    private static final int SCROLL = 20;
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private final Timeline scrollTimeline = new Timeline();
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnReload;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSaveAs;
    @FXML
    private CheckBox chkEnableCheat;
    @FXML
    private CheckBox chkEnableConsole;
    @FXML
    private CheckBox chkEnableDeveloper;
    @FXML
    private CheckBox chkEnableRollback;
    @FXML
    private CheckBox chkEnableWrite;
    private CodeConverter codeConverter;
    private FolderChooser gameChooser;
    @FXML
    private KeyText keyCheat;
    @FXML
    private KeyText keyConsole;
    @FXML
    private KeyText keyDeveloper;
    @FXML
    private KeyText keyWrite;
    @FXML
    private TreeTableColumn<DisplayItem, String> labelColumn;
    private ModelConverter modelConverter;
    @FXML
    private TreeTableColumn<DisplayItem, ModelType> modelTypeColumn;
    @FXML
    private TreeTableColumn<DisplayItem, String> nameColumn;
    private Preferences preferences;
    private ResourceBundle resources;
    private ScriptConverter scriptConverter;
    private double scrollDirection;
    @FXML
    private TextArea source;
    private Storage storage;
    private FileChooser storageChooser;
    @FXML
    private Tab tabBuilder;
    @FXML
    private Tab tabCreator;
    @FXML
    private TreeTableView<DisplayItem> tree;
    @FXML
    private TreeTableColumn<DisplayItem, VarType> typeColumn;
    @FXML
    private TreeTableColumn<DisplayItem, String> valueColumn;

    public Main() {
        FXLoad.loadComponent(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass()), this, this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }

    @SuppressWarnings("ObjectEquality")
    private static boolean isNotParent(@Nullable TreeItem<DisplayItem> parent, @Nullable TreeItem<DisplayItem> child) {
        TreeItem<DisplayItem> item = child;
        boolean result = true;
        while (result && item != null) {
            result = item.getParent() != parent;
            item = item.getParent();
        }
        return result;
    }

    private void attachTo(@NotNull TreeItem<DisplayItem> destItem, @NotNull TreeItem<DisplayItem> dragItem) {
        dragItem.getParent().getChildren().remove(dragItem);
        destItem.getChildren().add(dragItem);
        tree.getSelectionModel().select(dragItem);
    }

    private boolean canBeChildOfParent(@NotNull DisplayItem child, @NotNull DisplayItem parent) {
        child.getModelType();
        return parent.getModelType() == ModelType.MENU;
    }

    private boolean canEdit(@NotNull DisplayItem item, @Nullable String id) {
        ModelType modelType = item.getModelType();
        return !COLUMN_MODEL_TYPE.equals(id) &&
               (modelType == ModelType.VARIABLE ||
                modelType == ModelType.MENU && COLUMN_LABEL.equals(id));
    }

    private void addVariable(@NotNull TreeItem<DisplayItem> item) {
        NewVariableDialog dialog = new NewVariableDialog();
        dialog.initOwner(getStage());
        dialog.showAndWait().ifPresent(variable -> addItem(item, variable));
    }

    private void classAdd(@NotNull Styleable row, String className) {
        ObservableList<String> styleClass = row.getStyleClass();
        if (!styleClass.contains(className)) {
            styleClass.add(className);
        }
    }

    private void classAddRemove(@NotNull Styleable row, String classNameAdd, String classNameRemove) {
        classAdd(row, classNameAdd);
        classRemove(row, classNameRemove);
    }

    @SuppressWarnings("SameParameterValue")
    private void classAddRemoveAll(@NotNull Styleable row, String classNameAdd, String... classNameRemove) {
        classAdd(row, classNameAdd);
        classRemoveAll(row, classNameRemove);
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    private void classRemove(@NotNull Styleable row, String className) {
        ObservableList<String> styleClass = row.getStyleClass();
        while (styleClass.contains(className)) {
            styleClass.remove(className);
        }
    }

    private void classRemoveAll(@NotNull Styleable row, String... classNames) {
        for (String className : classNames) {
            classRemove(row, className);
        }
    }

    @NotNull
    private ContextMenu createContextMenuForRow(@NotNull TreeItem<DisplayItem> treeItem) {
        return new ContextMenu(
                createMenuItem("create-variable", treeItem, this::addVariable),
                createMenuItem("create-submenu", treeItem, this::addSubmenu),
                createMenuItem("remove", treeItem, this::removeItem));
    }

    private boolean cannotBlank(@NotNull DisplayItem item, @Nullable String id) {
        ModelType modelType = item.getModelType();
        return modelType == ModelType.MENU && COLUMN_LABEL.equals(id) ||
               modelType == ModelType.VARIABLE && COLUMN_NAME.equals(id);
    }

    @NotNull
    private MenuItem createMenuItem(@NotNull String key, @NotNull TreeItem<DisplayItem> treeItem,
                                    @NotNull Consumer<TreeItem<DisplayItem>> handler) {
        MenuItem item = new MenuItem(resources.getString(key));
        item.setOnAction(event -> handler.accept(treeItem));
        return item;
    }

    @NotNull
    private TreeTableRow<DisplayItem> createRow(TreeTableView<DisplayItem> table) {
        TreeTableRow<DisplayItem> row = new ContextMenuTreeTableRow();
        initializeDnD(row);
        row.indexProperty().addListener((observable, oldValue, newValue) ->
                updateRow(row, row.getTreeItem(), row.getIndex()));
        row.treeTableViewProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setOnScroll(scrollEvent ->
                        updateRow(row, row.getTreeItem(), row.getIndex()));
            }
        });
        row.hoverProperty().addListener((observable, oldValue, newValue) ->
                updateRow(row, row.getTreeItem(), row.getIndex()));
        return row;
    }

    @NotNull
    private FileChooser createStorageChooser(Path storagePath) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                resources.getString("extension-description"), EXTENSION);
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setSelectedExtensionFilter(filter);
        Optional.ofNullable(storagePath).ifPresent(path -> {
            fileChooser.setInitialDirectory(path.getParent().toFile());
            fileChooser.setInitialFileName(path.getFileName().toString());
        });
        return fileChooser;
    }

    @NotNull
    private FolderChooser createGameChooser(String gameDirectory) {
        FolderChooser directoryChooser = new FolderChooser();
        directoryChooser.setInitialDirectory(Paths.get(gameDirectory).toFile());
        return directoryChooser;
    }

    @SuppressWarnings({"ParameterHidesMemberVariable", "AccessOfSystemProperties"})
    @Override
    public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
        this.resources = resources;
        modelConverter = new ModelConverter();
        scriptConverter = new ScriptConverter();
        preferences = Preferences.userNodeForPackage(getClass());
        Settings settings = new Settings(preferences,
                chkEnableCheat.isSelected(), chkEnableConsole.isSelected(),
                chkEnableDeveloper.isSelected(), chkEnableWrite.isSelected(), chkEnableRollback.isSelected(),
                keyCheat.getDefaultCombination(), keyConsole.getDefaultCombination(),
                keyDeveloper.getDefaultCombination(), keyWrite.getDefaultCombination());
        initializeSettings(settings);
        codeConverter = new CodeConverter(FXContextFactory.currentContext(), settings, CodeConverter.SPACES);
        gameChooser = createGameChooser(preferences.get(PREF_GAME, System.getProperty("user.home")));
        initializeTabs();
        initializeTree();
        storage = new Storage();
        storageChooser = createStorageChooser(storage.getPath());
        initializeStorage(storage);
    }

    private void initializeCell(@NotNull TreeTableCell<DisplayItem, ?> cell) {
        cell.itemProperty().addListener((observable, oldValue, newValue) -> initializeCellAny(cell));
        cell.hoverProperty().addListener((observable, oldValue, newValue) -> initializeCellAny(cell));
        cell.parentProperty().addListener((observable, oldValue, newValue) -> initializeCellAny(cell));
    }

    private void initializeTree() {
        setRoot(DisplayItem.toTreeItem(DisplayItem.createRoot()));
        tree.setShowRoot(false);
        tree.setRowFactory(this::createRow);

        initializeColumnDecorated(modelTypeColumn, DisplayItem::modelTypeProperty, (cell, modelType) ->
                FXLoad.findResource(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass(),
                        Optional.ofNullable(modelType).map(type -> {
                            switch (type) {
                                case MENU:
                                    return ICON_MENU;
                                case VARIABLE:
                                    return ICON_VARIABLE;
                                default:
                                    throw new AppException("Wrong ModelType" + type);
                            }
                        }).orElse(ICON_EMPTY)), FXLoad.IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new)
                .ifPresent(cell::setGraphic));
        initializeColumnCombo(VarType.class, typeColumn, DisplayItem::typeProperty, true);
        initializeColumnText(labelColumn, DisplayItem::labelProperty);
        initializeColumnText(nameColumn, DisplayItem::nameProperty);
        initializeColumnText(valueColumn, DisplayItem::valueProperty);

        scrollTimeline.setCycleCount(Animation.INDEFINITE);
        scrollTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(SCROLL), "Scroll",
                e -> tree.lookupAll(".scroll-bar").stream()
                        .filter(ScrollBar.class::isInstance)
                        .map(ScrollBar.class::cast)
                        .filter(scrollBar -> Orientation.VERTICAL == scrollBar.getOrientation())
                        .findAny().ifPresent(scrollBar ->
                                scrollBar.setValue(Math.max(0.0, Math.min(1.0,
                                        scrollBar.getValue() + scrollDirection))))));
        tree.setOnDragExited(event -> {
            if (event.getY() > 0) {
                scrollDirection = 1.0 / tree.getExpandedItemCount();
            } else {
                scrollDirection = -1.0 / tree.getExpandedItemCount();
            }
            scrollTimeline.play();
        });
        tree.setOnDragEntered(event -> scrollTimeline.stop());
        tree.setOnDragDone(event -> scrollTimeline.stop());
    }

    @SuppressWarnings("SameParameterValue")
    private <E extends Enum<E>> void initializeColumnCombo(
            @NotNull Class<E> type,
            @NotNull TreeTableColumn<DisplayItem, E> column,
            @NotNull Function<DisplayItem, ObservableValue<E>> factory,
            boolean editable) {
        column.setEditable(editable);
        column.setCellValueFactory(features -> factory.apply(features.getValue().getValue()));
        column.setCellFactory(col -> {
            TreeTableCell<DisplayItem, E> cell =
                    ComboBoxTreeTableCell.<DisplayItem, E>forTreeTableColumn(type.getEnumConstants()).call(col);
            initializeCell(cell);
            return cell;
        });
        initializeColumnValidate(column);
    }

    @NotNull
    @SuppressWarnings("ConstantExpression")
    private DROPPING determineDropping(@NotNull TreeTableRow<DisplayItem> row, @NotNull DragEvent event) {
        double ratio = event.getY() / row.getLayoutBounds().getHeight();
        if (ratio < BOND) {
            return DROPPING.ABOVE;
        }
        if (ratio > 1 - BOND) {
            return DROPPING.BELOW;
        }
        return DROPPING.ONTO;
    }

    private void doLoad(Path path) {
        tree.setRoot(modelConverter.toTree(storage.load(path)));
        storage.setPath(path);
        updateScript();
    }

    private <T> void initializeColumnValidate(@NotNull TreeTableColumn<DisplayItem, T> column) {
        SimpleObjectProperty<DisplayItem> storedItem = new SimpleObjectProperty<>();
        column.setOnEditStart(event -> {
            TreeItem<DisplayItem> treeItem = event.getRowValue();
            if (treeItem == null) {
                storeClear(storedItem);
            } else if (!canEdit(treeItem.getValue(), column.getId())) {
                storeCopy(storedItem, treeItem);
            } else {
                storeCopy(storedItem, treeItem);
            }
        });
        column.setOnEditCommit(event -> {
            TreeItem<DisplayItem> treeItem = event.getRowValue();
            if (treeItem == null) {
                storeClear(storedItem);
            } else if (!canEdit(treeItem.getValue(), column.getId())) {
                storeReset(storedItem, treeItem);
            } else {
                T newValue = event.getNewValue();
                TreeTableView<DisplayItem> table = event.getTreeTableView();
                if (newValue instanceof String && ((String) newValue).trim().isBlank() &&
                    cannotBlank(treeItem.getValue(), column.getId())) {
                    storeReset(storedItem, treeItem);
                    storeUpdate(table, column, treeItem, event.getOldValue());
                } else {
                    storeUpdate(table, column, treeItem, newValue);
                    storeClear(storedItem);
                }
            }
        });
        column.setOnEditCancel(event -> storeClear(storedItem));
    }

    private void initializeDnD(@NotNull TreeTableRow<DisplayItem> row) {
        row.setOnDragDetected(event -> {
            if (!row.isEmpty()) {
                Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(row.snapshot(null, null));
                Map<DataFormat, Object> content = new ClipboardContent();
                content.put(SERIALIZED_MIME_TYPE, row.getIndex());
                dragboard.setContent(content);
                event.consume();
            }
        });
        row.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                int index = tree.getRow(dragItem);
                TreeItem<DisplayItem> destItem = row.isEmpty() ? tree.getRoot() : row.getTreeItem();
                if (row.getIndex() != index) {
                    if (isNotParent(dragItem, destItem)) {
                        DROPPING dropping = determineDropping(row, event);
                        if (canBeChildOfParent(dragItem.getValue(), destItem.getValue()) && dropping == DROPPING.ONTO) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        } else if (dropping == DROPPING.ABOVE || dropping == DROPPING.BELOW) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        }
                    }
                }
            }
        });
        row.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                int index = tree.getRow(dragItem);
                TreeItem<DisplayItem> destItem = row.isEmpty() ? tree.getRoot() : row.getTreeItem();
                int rowIndex = row.getIndex();
                if (rowIndex != index) {
                    if (isNotParent(dragItem, destItem)) {
                        DROPPING dropping = determineDropping(row, event);
                        if (canBeChildOfParent(dragItem.getValue(), destItem.getValue()) && dropping == DROPPING.ONTO) {
                            attachTo(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateRow(row, destItem, rowIndex);
                        } else if (dropping == DROPPING.ABOVE) {
                            putAbove(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateRow(row, destItem, rowIndex);
                        } else if (dropping == DROPPING.BELOW) {
                            putBelow(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateRow(row, destItem, rowIndex);
                        }
                    }
                }
            }
        });
    }

    private void initializeSettings(@NotNull Settings settings) {
        chkEnableCheat.selectedProperty().setValue(settings.enableCheatProperty().getValue());
        chkEnableConsole.selectedProperty().setValue(settings.enableConsoleProperty().getValue());
        chkEnableDeveloper.selectedProperty().setValue(settings.enableDeveloperProperty().getValue());
        chkEnableWrite.selectedProperty().setValue(settings.enableWriteProperty().getValue());
        keyCheat.defaultCombinationProperty().setValue(settings.keyCheatProperty().getValue());
        keyConsole.defaultCombinationProperty().setValue(settings.keyConsoleProperty().getValue());
        keyDeveloper.defaultCombinationProperty().setValue(settings.keyDeveloperProperty().getValue());
        keyWrite.defaultCombinationProperty().setValue(settings.keyWriteProperty().getValue());

        settings.enableCheatProperty().bind(chkEnableCheat.selectedProperty());
        settings.enableConsoleProperty().bind(chkEnableConsole.selectedProperty());
        settings.enableDeveloperProperty().bind(chkEnableDeveloper.selectedProperty());
        settings.enableWriteProperty().bind(chkEnableWrite.selectedProperty());
        settings.keyCheatProperty().bind(keyCheat.combinationProperty());
        settings.keyConsoleProperty().bind(keyConsole.combinationProperty());
        settings.keyDeveloperProperty().bind(keyDeveloper.combinationProperty());
        settings.keyWriteProperty().bind(keyWrite.combinationProperty());

        keyCheat.disableProperty().bind(Bindings.not(chkEnableCheat.selectedProperty()));
        keyConsole.disableProperty().bind(Bindings.not(chkEnableConsole.selectedProperty()));
        keyDeveloper.disableProperty().bind(Bindings.not(chkEnableDeveloper.selectedProperty()));
        keyWrite.disableProperty().bind(Bindings.not(chkEnableWrite.selectedProperty()));
    }

    private void doSave(Path path) {
        storage.saveAs(path, modelConverter.toModel(tree.getRoot()));
        storage.setPath(path);
    }

    private void initializeTabs() {
        tabCreator.selectedProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                updateScript();
            }
        });
        tabBuilder.selectedProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                updateTree();
            }
        });
    }

    private void doTemplate() {
        setRoot(modelConverter.toTree(ModelTemplate.getTemplateTree()));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    private void initializeStorage(@NotNull Storage storage) {
        BooleanBinding nullBinding = Bindings.isNull((ObservableObjectValue<?>) storage.pathProperty());
        btnReload.disableProperty().bind(nullBinding);
        btnSave.disableProperty().bind(nullBinding);
    }

    private void initializeCellAny(@NotNull TreeTableCell<DisplayItem, ?> cell) {
        TreeTableRow<DisplayItem> row = cell.getTreeTableRow();
        if (row == null) {
            cell.setEditable(false);
        } else {
            TreeItem<DisplayItem> treeItem = row.getTreeItem();
            if (treeItem == null) {
                cell.setEditable(false);
            } else {
                DisplayItem item = treeItem.getValue();
                cell.setEditable(item != null && canEdit(item, cell.getTableColumn().getId()));
            }
            cell.indexProperty().addListener((observable, oldValue, newValue) ->
                    updateRow(row, row.getTreeItem(), row.getIndex()));
        }
    }

    private <E extends Enum<E>> void initializeColumnDecorated(
            @NotNull TreeTableColumn<DisplayItem, E> column,
            @NotNull Function<DisplayItem, ObservableValue<E>> factory,
            @NotNull BiConsumer<TreeTableCell<DisplayItem, E>, E> decorator) {
        column.setEditable(false);
        column.setCellValueFactory(features -> factory.apply(features.getValue().getValue()));
        column.setCellFactory(col -> {
            TreeTableCell<DisplayItem, E> cell = new DecoratedTreeTableCell<>(decorator);
            initializeCell(cell);
            return cell;
        });
        initializeColumnValidate(column);
    }

    @NotNull
    private Stage getStage() {
        return (Stage) tree.getScene().getWindow();
    }

    private void doClear() {
        tree.getRoot().getChildren().clear();
        updateScript();
    }

    private void updateScript() {
        source.setText(String.join("\n",
                scriptConverter.toScript(modelConverter.toModel(tree.getRoot()))));
    }

    private void initializeColumnText(@NotNull TreeTableColumn<DisplayItem, String> column,
                                      @NotNull Function<DisplayItem, ObservableValue<String>> factory) {
        column.setEditable(true);
        column.setCellValueFactory(features -> factory.apply(features.getValue().getValue()));
        column.setCellFactory(list -> {
            TreeTableCell<DisplayItem, String> cell = TextFieldTreeTableCell.<DisplayItem>forTreeTableColumn()
                    .call(list);
            initializeCell(cell);
            return cell;
        });
        initializeColumnValidate(column);
    }

    private void putClipboard(@NotNull String content) {
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
        return codeConverter.toCode(modelConverter.toModel(tree.getRoot()));
    }

    private boolean isFirstChild(@Nullable TreeItem<DisplayItem> treeItem) {
        if (treeItem == null) {
            return true;
        }
        TreeItem<DisplayItem> parent = treeItem.getParent();
        if (parent == null) {
            return true;
        }
        ObservableList<TreeItem<DisplayItem>> children = parent.getChildren();
        return children.indexOf(treeItem) == 0;
    }

    private boolean isLastChild(@Nullable TreeItem<DisplayItem> treeItem) {
        if (treeItem == null) {
            return false;
        }
        TreeItem<DisplayItem> parent = treeItem.getParent();
        if (parent == null) {
            return false;
        }
        ObservableList<TreeItem<DisplayItem>> children = parent.getChildren();
        return children.indexOf(treeItem) == children.size() - 1;
    }

    private static boolean isGameDirectory(@Nullable Path path) {
        if (path != null) {
            if (Files.exists(path)) {
                Path renpy = path.resolve("renpy");
                if (Files.exists(renpy) && Files.isDirectory(renpy)) {
                    Path game = path.resolve("game");
                    if (Files.exists(game) && Files.isDirectory(game)) {
                        Path lib = path.resolve("lib");
                        return Files.exists(lib) && Files.isDirectory(lib);
                    }
                }
            }
        }
        return false;
    }

    private void storeGamePath(Path gamePath) {
        preferences.put(PREF_GAME, gamePath.toFile().getAbsolutePath());
        gameChooser.setInitialDirectory(gamePath.toFile());
    }

    @FXML
    void onClear(@NotNull ActionEvent actionEvent) {
        if (tree.getRoot().getChildren().isEmpty() ||
            FXDialogs.confirm(FXContextFactory.currentContext(), getStage(), resources.getString("clear-confirm"))) {
            doClear();
        }
    }

    @FXML
    void onGenerate(@NotNull ActionEvent actionEvent) {
        Stage stage = getStage();
        try {
            putClipboard(generateCodeString());
            FXDialogs.alert(FXContextFactory.currentContext(), stage, resources.getString("success-generate"));
        } catch (RuntimeException e) {
            FXDialogs.error(FXContextFactory.currentContext(), stage, resources.getString("error-generate"), e);
        }
    }

    @FXML
    void onInstall(@NotNull ActionEvent actionEvent) {
        chooseGameDirectory().ifPresent(gamePath -> {
            Stage stage = getStage();
            if (isGameDirectory(gamePath)) {
                try {
                    Files.writeString(gamePath.resolve("game").resolve(INSTALL_NAME), generateCodeString());
                    FXDialogs.alert(FXContextFactory.currentContext(), stage, resources.getString("success-install"));
                    storeGamePath(gamePath);
                } catch (IOException e) {
                    FXDialogs.error(FXContextFactory.currentContext(), stage, resources.getString("error-write"), e);
                }
            } else {
                FXDialogs.error(FXContextFactory.currentContext(), stage, resources.getString("error-not-game"));
            }
        });
    }

    @FXML
    void onLoad(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storageChooser.showOpenDialog(getStage())).map(File::toPath).ifPresent(path -> {
            if (tree.getRoot().getChildren().isEmpty() ||
                FXDialogs.confirm(FXContextFactory.currentContext(), getStage(), resources.getString("load-confirm"))) {
                try {
                    doLoad(path);
                } catch (RuntimeException e) {
                    FXDialogs.error(FXContextFactory.currentContext(), getStage(), resources.getString("error-load"), e);
                }
            }
        });
    }

    @FXML
    void onReload(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            if (tree.getRoot().getChildren().isEmpty() ||
                FXDialogs.confirm(FXContextFactory.currentContext(), getStage(), resources.getString("reload-confirm"))) {
                try {
                    doLoad(path);
                } catch (RuntimeException e) {
                    FXDialogs.error(FXContextFactory.currentContext(), getStage(), resources.getString("error-load"), e);
                }
            }
        });
    }

    @FXML
    void onSave(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storage.getPath()).ifPresent(path -> {
            FXContext context = FXContextFactory.currentContext();
            if (Files.exists(path) || FXDialogs.confirm(context, getStage(), resources.getString("save-confirm"))) {
                try {
                    doSave(path);
                } catch (AppException e) {
                    FXDialogs.error(context, getStage(), resources.getString("error-save"), e);
                }
            }
        });
    }

    @FXML
    void onSaveAs(@NotNull ActionEvent actionEvent) {
        Optional.ofNullable(storageChooser.showSaveDialog(getStage())).map(File::toPath).ifPresent(path -> {
            FXContext context = FXContextFactory.currentContext();
            if (Files.exists(path) || FXDialogs.confirm(context, getStage(), resources.getString("save-as-confirm"))) {
                try {
                    doSave(path);
                } catch (AppException e) {
                    FXDialogs.error(context, getStage(), resources.getString("error-save"), e);
                }
            }
        });
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    private Optional<Path> chooseGameDirectory() {
        Stage stage = getStage();
        if (gameChooser.getOwner() == null) {
            gameChooser.initOwner(getStage());
        }
        Path initialDirectory = gameChooser.getInitialDirectory().toPath();
        while (!Files.exists(initialDirectory)) {
            initialDirectory = initialDirectory.getParent();
            gameChooser.setInitialDirectory(initialDirectory.toFile());
        }
        File file = gameChooser.showDialog(stage);
        return Optional.ofNullable(file).map(File::toPath);
    }

    @FXML
    void onResetKeys(@NotNull ActionEvent actionEvent) {
        for (KeyText keyText : new KeyText[]{keyCheat, keyConsole, keyWrite}) {
            keyText.reset();
        }
    }

    @FXML
    void onSubmenu(@NotNull ActionEvent actionEvent) {
        addSubmenu(tree.getRoot());
    }

    @FXML
    void onTemplate(@NotNull ActionEvent actionEvent) {
        FXContext context = FXContextFactory.currentContext();
        if (tree.getRoot().getChildren().isEmpty() ||
            FXDialogs.confirm(context, getStage(), resources.getString("template-confirm"))) {
            try {
                doTemplate();
//                FXDialogs.alert(getStage(), resources.getString("success-template"));
            } catch (IllegalArgumentException | IllegalStateException e) {
                FXDialogs.error(context, getStage(), resources.getString("error-malformed-script"), e);
            }
            updateScript();
        }
    }

    private void addSubmenu(@Nullable TreeItem<DisplayItem> item) {
        SubmenuDialog dialog = new SubmenuDialog();
        dialog.initOwner(getStage());
        dialog.showAndWait().ifPresent(submenu -> addItem(item, submenu));
    }

    private void setRoot(@NotNull TreeItem<DisplayItem> root) {
        root.setExpanded(true);
        tree.setRoot(root);
    }

    @FXML
    void onVariable(@NotNull ActionEvent actionEvent) {
        addVariable(tree.getRoot());
    }

    private void addItem(@Nullable TreeItem<DisplayItem> item, @NotNull DisplayItem variable) {
        TreeItem<DisplayItem> treeItem = item == null ? tree.getRoot() : item;
        treeItem.getChildren().add(DisplayItem.toTreeItem(variable));
    }

    private void putAbove(@NotNull TreeItem<DisplayItem> destItem, @NotNull TreeItem<DisplayItem> dragItem) {
        ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        dragItem.getParent().getChildren().remove(dragItem);
        destSiblings.add(destSiblings.indexOf(destItem), dragItem);
        tree.getSelectionModel().select(dragItem);
    }

    private void putBelow(@NotNull TreeItem<DisplayItem> destItem, @NotNull TreeItem<DisplayItem> dragItem) {
        ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        int index = destSiblings.indexOf(destItem);
        dragItem.getParent().getChildren().remove(dragItem);
        if (index < destSiblings.size() - 1) {
            destSiblings.add(index + 1, dragItem);
        } else {
            destSiblings.add(dragItem);
        }
        tree.getSelectionModel().select(dragItem);
    }

    private void removeItem(@NotNull TreeItem<DisplayItem> treeItem) {
        if (FXDialogs.confirm(FXContextFactory.currentContext(), getStage(), resources.getString("remove-confirm"))) {
            treeItem.getParent().getChildren().remove(treeItem);
        }
    }

    private void setRowSignalDecorations(@NotNull Styleable row,
                                         @Nullable TreeItem<DisplayItem> treeItem, int rowIndex) {
        classRemoveAll(row, CLASS_ODD, CLASS_EVEN, CLASS_WARN, CLASS_DANGER);
        if (treeItem != null) {
            DisplayItem item = treeItem.getValue();
            if (item != null) {
                if (rowIndex % 2 == 0) {
                    classAddRemove(row, CLASS_EVEN, CLASS_ODD);
                } else {
                    classAddRemove(row, CLASS_ODD, CLASS_EVEN);
                }
                switch (item.getModelType()) {
                    case VARIABLE: {
                        int size = treeItem.getParent() == null ? 0 : treeItem.getParent().getChildren().size();
                        setRowSignalDecorations0(row, size);
                        break;
                    }
                    case MENU:
                        int size = treeItem.getChildren().size();
                        setRowSignalDecorations0(row, size);
                        break;
                    default:
                        throw new IllegalArgumentException(String.valueOf(item.getModelType()));
                }
            }
        }
    }

    private void setRowSignalDecorations0(@NotNull Styleable row, int size) {
        if (size <= MENU_SIZE_WARN) {
            classRemoveAll(row, CLASS_WARN, CLASS_DANGER);
        }
        if (size > MENU_SIZE_WARN && size < MENU_SIZE_DANGER) {
            classAddRemove(row, CLASS_WARN, CLASS_DANGER);
        }
        if (size >= MENU_SIZE_DANGER) {
            classAddRemove(row, CLASS_DANGER, CLASS_WARN);
        }
    }

    private void setRowSplitDecorations(@NotNull Styleable row,
                                        @Nullable TreeItem<DisplayItem> treeItem) {
        classRemoveAll(row, CLASS_VARIABLE, CLASS_MENU, CLASS_FIRST_CHILD, CLASS_LAST_CHILD);
        if (treeItem != null) {
            switch (treeItem.getValue().getModelType()) {
                case MENU:
                    classAddRemoveAll(row, CLASS_MENU, CLASS_VARIABLE, CLASS_FIRST_CHILD, CLASS_LAST_CHILD);
                    break;
                case VARIABLE:
                    classAddRemove(row, CLASS_VARIABLE, CLASS_MENU);
                    if (isFirstChild(treeItem)) {
                        classAdd(row, CLASS_FIRST_CHILD);
                    } else {
                        classRemove(row, CLASS_FIRST_CHILD);
                    }
                    if (isLastChild(treeItem)) {
                        classAdd(row, CLASS_LAST_CHILD);
                    } else {
                        classRemove(row, CLASS_LAST_CHILD);
                    }
                    break;
                default:
                    throw new IllegalStateException(String.valueOf(treeItem.getValue().getModelType()));
            }
        }
    }

    private void storeClear(@NotNull WritableValue<DisplayItem> storedItem) {
        storedItem.setValue(null);
    }

    private void storeCopy(@NotNull WritableValue<DisplayItem> storedItem, @NotNull TreeItem<DisplayItem> rowValue) {
        storedItem.setValue(rowValue.getValue().copyOf());
    }

    private void storeReset(@NotNull SimpleObjectProperty<DisplayItem> storedItem,
                            @NotNull TreeItem<DisplayItem> rowValue) {
        rowValue.setValue(storedItem.getValue());
        storeClear(storedItem);
    }

    @SuppressWarnings("unchecked")
    private <T> void storeUpdate(@NotNull TreeTableView<DisplayItem> table,
                                 @NotNull TreeTableColumn<DisplayItem, T> column,
                                 @Nullable TreeItem<DisplayItem> row, T newValue) {
        ObservableValue<T> observable = column.getCellValueFactory()
                .call(new TreeTableColumn.CellDataFeatures<>(table, column, row));
        if (observable instanceof WritableValue) {
            ((WritableValue<T>) observable).setValue(newValue);
        }
    }

    private void updateRow(@NotNull TreeTableRow<DisplayItem> row, @Nullable TreeItem<DisplayItem> treeItem,
                           int rowIndex) {
        Optional.ofNullable(treeItem).ifPresent(item -> row.setContextMenu(createContextMenuForRow(treeItem)));

        Platform.runLater(() -> {
            setRowSignalDecorations(row, treeItem, rowIndex);
            setRowSplitDecorations(row, treeItem);
        });
    }

    private void updateTree() {
        try {
            tree.setRoot(modelConverter.toTree(
                    scriptConverter.fromScript(Arrays.asList(source.getText().split("\n")))));
        } catch (IllegalArgumentException | IllegalStateException e) {
            FXContext context = FXContextFactory.currentContext();
            FXDialogs.error(context, getStage(), resources.getString("error-malformed-script"), e);
        }
    }

    private static final class DecoratedTreeTableCell<E extends Enum<E>> extends TreeTableCell<DisplayItem, E> {
        private final BiConsumer<TreeTableCell<DisplayItem, E>, E> decorator;

        private DecoratedTreeTableCell(@NotNull BiConsumer<TreeTableCell<DisplayItem, E>, E> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(E item, boolean empty) {
            super.updateItem(item, empty);
            decorator.accept(this, empty ? null : item);
        }
    }

    private final class ContextMenuTreeTableRow extends TreeTableRow<DisplayItem> {
        @Override
        protected void updateItem(DisplayItem item, boolean empty) {
            super.updateItem(item, empty);
            updateRow(this, empty ? tree.getRoot() : getTreeItem(), getIndex());
        }
    }
}
