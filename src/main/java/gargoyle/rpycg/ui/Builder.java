package gargoyle.rpycg.ui;

import gargoyle.rpycg.RPyCG;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXComponent;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelTemplate;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.service.ModelConverter;
import gargoyle.rpycg.ui.model.DROPPING;
import gargoyle.rpycg.ui.model.DisplayItem;
import gargoyle.rpycg.ui.model.FULLNESS;
import gargoyle.rpycg.util.Classes;
import gargoyle.rpycg.util.TreeItemWalker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Builder extends ScrollPane implements Initializable {
    private static final double BOND = 0.3;
    private static final String CLASS_DANGER = "danger";
    private static final String CLASS_EVEN = "even";
    private static final String CLASS_FIRST_CHILD = "first-child";
    private static final String CLASS_LAST_CHILD = "last-child";
    private static final String CLASS_MENU = "menu";
    private static final String CLASS_ODD = "odd";
    private static final String CLASS_VARIABLE = "variable";
    private static final String CLASS_WARN = "warn";
    private static final String ICON_DELETE = "icons/clear";
    private static final String ICON_EMPTY = "icons/empty";
    private static final String ICON_MENU = "icons/menu";
    private static final String ICON_TEMPLATE = "icons/template";
    private static final String ICON_VARIABLE = "icons/var";
    private static final String KEY_DEBUG = "debug";
    private static final String LC_CLOSE = "close";
    private static final String LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script";
    private static final String LC_REMOVE_CONFIRM = "remove-confirm";
    private static final String LC_REMOVE_CONFIRM_CANCEL = "remove-confirm-cancel";
    private static final String LC_REMOVE_CONFIRM_OK = "remove-confirm-ok";
    private static final String LC_REPORT = "report";
    private static final String LC_TEMPLATE = "template";
    private static final String LC_TEMPLATE_CONFIRM = "template-confirm";
    private static final String LC_TEMPLATE_CONFIRM_CANCEL = "template-confirm-cancel";
    private static final String LC_TEMPLATE_CONFIRM_OK = "template-confirm-ok";
    private static final String LC_TEMPLATE_TOOLTIP = "template-tooltip";
    private static final MenuItem[] MENU_ITEMS = new MenuItem[0];
    private static final int SCROLL = 20;
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private final SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
    private final FXComponent<Builder, ScrollPane> component;
    private final ModelConverter modelConverter;
    private final Timeline scrollTimeline = new Timeline();
    private ResourceBundle resources;
    private double scrollDirection;
    @FXML
    private TreeView<DisplayItem> tree;

    public Builder() {
        modelConverter = new ModelConverter();
        component = FXContextFactory.currentContext().<Builder, ScrollPane>loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, Builder.class.getName()));
    }

    private static boolean canBeChildOfParent(DisplayItem child, DisplayItem parent) {
        child.getModelType();
        return parent.getModelType() == ModelType.MENU;
    }

    private static FULLNESS getFullness(TreeItem<DisplayItem> treeItem) {
        DisplayItem item = treeItem.getValue();
        switch (item.getModelType()) {
            case VARIABLE:
                return FULLNESS.determineFullness(treeItem.getParent() == null ? 0
                        : treeItem.getParent().getChildren().size());
            case MENU:
                return FULLNESS.determineFullness(treeItem.getChildren().size());
            default:
                throw new IllegalArgumentException(String.valueOf(item.getModelType()));
        }
    }

    private static boolean isFirstChild(TreeItem<DisplayItem> treeItem) {
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

    private static boolean isLastChild(TreeItem<DisplayItem> treeItem) {
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

    @SuppressWarnings("ObjectEquality")
    private static boolean isNotParent(TreeItem<DisplayItem> parent, TreeItem<DisplayItem> child) {
        TreeItem<DisplayItem> item = child;
        boolean result = true;
        while (result && item != null) {
            result = item.getParent() != parent;
            item = item.getParent();
        }
        return result;
    }

    private static void setCellSignalDecorations(Styleable cell, TreeItem<DisplayItem> treeItem) {
        if (treeItem != null && treeItem.getValue() != null) {
            switch (getFullness(treeItem)) {
                case NORMAL:
                    Classes.classRemoveAll(cell, CLASS_WARN, CLASS_DANGER);
                    break;
                case ALMOST:
                    Classes.classAddRemove(cell, CLASS_WARN, CLASS_DANGER);
                    break;
                case FULL:
                    Classes.classAddRemove(cell, CLASS_DANGER, CLASS_WARN);
                    break;
            }
        } else {
            Classes.classRemoveAll(cell, CLASS_WARN, CLASS_DANGER);
        }
    }

    private static void setCellSplitDecorations(Styleable cell,
                                                TreeItem<DisplayItem> treeItem) {
        Classes.classRemoveAll(cell, CLASS_VARIABLE, CLASS_MENU, CLASS_FIRST_CHILD, CLASS_LAST_CHILD);
        if (treeItem != null) {
            DisplayItem displayItem = treeItem.getValue();
            if (displayItem != null) {
                switch (displayItem.getModelType()) {
                    case MENU:
                        Classes.classAddRemoveAll(cell, CLASS_MENU,
                                CLASS_VARIABLE, CLASS_FIRST_CHILD, CLASS_LAST_CHILD);
                        break;
                    case VARIABLE:
                        Classes.classAddRemove(cell, CLASS_VARIABLE, CLASS_MENU);
                        if (isFirstChild(treeItem)) {
                            Classes.classAdd(cell, CLASS_FIRST_CHILD);
                        } else {
                            Classes.classRemove(cell, CLASS_FIRST_CHILD);
                        }
                        if (isLastChild(treeItem)) {
                            Classes.classAdd(cell, CLASS_LAST_CHILD);
                        } else {
                            Classes.classRemove(cell, CLASS_LAST_CHILD);
                        }
                        break;
                    default:
                        throw new IllegalStateException(String.valueOf(displayItem.getModelType()));
                }
            }
        }
    }

    private static void setCellZebraDecorations(Styleable cell,
                                                TreeItem<DisplayItem> treeItem, int cellIndex) {
        Classes.classRemoveAll(cell, CLASS_ODD, CLASS_EVEN);
        if (treeItem != null && treeItem.getValue() != null) {
            if (cellIndex % 2 == 0) {
                Classes.classAddRemove(cell, CLASS_EVEN, CLASS_ODD);
            } else {
                Classes.classAddRemove(cell, CLASS_ODD, CLASS_EVEN);
            }
        }
    }

    public void addRootMenu() {
        addMenu(getRoot());
    }

    private void addMenu(TreeItem<DisplayItem> item) {
        MenuDialog dialog = new MenuDialog();
        dialog.setKnown(getKnownNames(""));
        getStage().ifPresent(dialog::initOwner);
        dialog.showAndWait().ifPresent(menu -> addItem(item, menu, true));
    }

    private TreeItem<DisplayItem> getRoot() {
        return tree.getRoot();
    }

    private Set<String> getKnownNames(String allow) {
        Set<String> known = new HashSet<>(tree.getChildrenUnmodifiable().size());
        TreeItemWalker.visitItems(tree, displayItem -> {
            String name = displayItem.getName();
            if (!name.equalsIgnoreCase(allow)) {
                known.add(name);
            }
        });
        return known;
    }

    private Optional<Stage> getStage() {
        return FXUtil.findStage(tree);
    }

    private void addItem(TreeItem<DisplayItem> item, DisplayItem displayItem, boolean expanded) {
        TreeItem<DisplayItem> treeItem = item == null ? tree.getRoot() : item;
        TreeItem<DisplayItem> newItem = DisplayItem.toTreeItem(displayItem, expanded);
        treeItem.getChildren().add(newItem);
        changed.setValue(true);
        selectItem(newItem);
    }

    private void selectItem(TreeItem<DisplayItem> item) {
        TreeItem<DisplayItem> treeItem = item;
        while (treeItem != null) {
            treeItem.setExpanded(treeItem.getValue() != null && treeItem.getValue().getModelType() == ModelType.MENU);
            treeItem = treeItem.getParent();
        }
        tree.getSelectionModel().select(item);
        tree.scrollTo(tree.getRow(item));
    }

    public void addRootVariable() {
        addVariable(getRoot());
    }

    private void addVariable(TreeItem<DisplayItem> item) {
        VariableDialog dialog = new VariableDialog();
        getStage().ifPresent(dialog::initOwner);
        dialog.showAndWait().ifPresent(variable -> addItem(item, variable, false));
    }

    private void attachTo(TreeItem<DisplayItem> destItem, TreeItem<DisplayItem> dragItem) {
        dragItem.getParent().getChildren().remove(dragItem);
        destItem.getChildren().add(dragItem);
        selectItem(dragItem);
    }

    public SimpleBooleanProperty changedProperty() {
        return changed;
    }

    public void clearAll() {
        changed.setValue(shouldClearRoot());
    }

    private ContextMenu createContextMenuForCell(TreeItem<DisplayItem> treeItem) {
        List<MenuItem> menuItems = FXCollections.observableArrayList();
        if (treeItem != null) {
            DisplayItem displayItem = treeItem.getValue();
            if (displayItem != null) {
                ModelType modelType = displayItem.getModelType();
                if (modelType == ModelType.VARIABLE) {
                    menuItems.add(createMenuItem(ICON_VARIABLE, "edit-variable", treeItem, this::editVariable));
                }
                if (modelType == ModelType.MENU) {
                    menuItems.add(createMenuItem(ICON_MENU, "edit-menu", treeItem, this::editMenu));
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(ICON_VARIABLE, "create-variable", treeItem, this::addVariable));
                    menuItems.add(createMenuItem(ICON_MENU, "create-menu", treeItem, this::addMenu));
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(ICON_MENU, "split-menu", treeItem, this::splitMenu));
                }
                if (treeItem.getParent() != null) {
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(ICON_DELETE, "remove", treeItem, this::removeItem));
                }
                return new ContextMenu(menuItems.toArray(MENU_ITEMS));
            }
        }
        menuItems.add(new SeparatorMenuItem());
        TreeItem<DisplayItem> rootItem = tree.getRoot();
        menuItems.add(createMenuItem(ICON_VARIABLE, "create-variable", rootItem, this::addVariable));
        menuItems.add(createMenuItem(ICON_MENU, "create-menu", rootItem, this::addMenu));
        return new ContextMenu(menuItems.toArray(MENU_ITEMS));
    }

    private TreeCell<DisplayItem> createDisplayItemTreeCell(TreeView<DisplayItem> treeView) {
        TreeCell<DisplayItem> treeCell = new DisplayItemTreeCell((cell, displayItem) -> {
            cell.setText(Optional.ofNullable(displayItem)
                    .map(DisplayItem::getLabel).filter(s -> !s.isBlank()).orElseGet(() ->
                            Optional.ofNullable(displayItem).map(DisplayItem::getName).orElse("")));
            component.findResource(Optional.ofNullable(displayItem).map(DisplayItem::getModelType).map(type -> {
                switch (type) {
                    case MENU:
                        return ICON_MENU;
                    case VARIABLE:
                        return ICON_VARIABLE;
                    default:
                        return ICON_EMPTY;
                }
            }).orElse(ICON_EMPTY), FXConstants.EXT_IMAGES)
                    .map(URL::toExternalForm).map(ImageView::new)
                    .ifPresent(cell::setGraphic);
            updateCell(cell, cell.getTreeItem(), cell.getIndex());
        });
        initializeDnD(treeCell);
        treeCell.indexProperty().addListener((observable, oldValue, newValue) ->
                updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
        treeCell.treeViewProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setOnScroll(scrollEvent ->
                        updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
            }
        });
        treeCell.hoverProperty().addListener((observable, oldValue, newValue) ->
                updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
        return treeCell;
    }

    private MenuItem createMenuItem(String graphicName,
                                    String key,
                                    TreeItem<DisplayItem> treeItem,
                                    Consumer<TreeItem<DisplayItem>> handler) {
        MenuItem item = new MenuItem(resources.getString(key), Optional.ofNullable(graphicName).flatMap(location ->
                component.findResource(location, FXConstants.EXT_IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new))
                .orElse(null));
        item.setOnAction(event -> handler.accept(treeItem));
        return item;
    }

    public Node createPlaceHolder(ResourceBundle resources) {
        FXContext context = FXContextFactory.currentContext();
        Button placeholder = new Button(resources.getString(LC_TEMPLATE),
                context.findResource(context.getBaseName(Builder.class, ICON_TEMPLATE), FXConstants.EXT_IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new).orElse(null));
        placeholder.setTooltip(new Tooltip(resources.getString(LC_TEMPLATE_TOOLTIP)));
        placeholder.setOnAction(this::onTemplate);
        return placeholder;
    }

    private void doTemplate() {
        Optional.ofNullable(FXContextFactory.currentContext().getParameters())
                .map(Application.Parameters::getNamed)
                .map(map -> map.get(KEY_DEBUG))
                .ifPresentOrElse((s) -> updateTree(ModelTemplate.getTestTemplateTree()),
                        () -> updateTree(ModelTemplate.getTemplateTree()));
    }

    private void editMenu(TreeItem<DisplayItem> item) {
        MenuDialog dialog = new MenuDialog();
        dialog.setKnown(getKnownNames(item == null || item.getValue() == null ? "" : item.getValue().getName()));
        getStage().ifPresent(dialog::initOwner);
        if (item != null) {
            dialog.setDisplayItem(item.getValue());
        }
        dialog.showAndWait().ifPresent(menu -> replaceItem(item, menu));
    }

    private void editVariable(TreeItem<DisplayItem> item) {
        VariableDialog dialog = new VariableDialog();
        getStage().ifPresent(dialog::initOwner);
        dialog.setDisplayItem(item.getValue());
        dialog.showAndWait().ifPresent(variable -> replaceItem(item, variable));
    }

    public ModelItem getModel() {
        return modelConverter.toModel(tree.getRoot());
    }

    public void setModel(ModelItem rootItem) {
        FXRun.runLater(() -> changed.setValue(shouldUpdateTree(modelConverter.toTree(rootItem))));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES,
                location.toExternalForm());
        initializeTree(createPlaceHolder(resources));
    }

    private void initializeDnD(TreeCell<DisplayItem> cell) {
        cell.setOnDragDetected(event -> {
            if (!cell.isEmpty()) {
                Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(cell.snapshot(null, null));
                Map<DataFormat, Object> content = new ClipboardContent();
                content.put(SERIALIZED_MIME_TYPE, cell.getIndex());
                dragboard.setContent(content);
                event.consume();
            }
        });
        cell.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                int index = tree.getRow(dragItem);
                TreeItem<DisplayItem> destItem = cell.isEmpty() ? tree.getRoot() : cell.getTreeItem();
                if (cell.getIndex() != index) {
                    if (isNotParent(dragItem, destItem)) {
                        DROPPING dropping = DROPPING.determineDropping(cell, event, BOND);
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
        cell.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                int index = tree.getRow(dragItem);
                TreeItem<DisplayItem> destItem = cell.isEmpty() ? tree.getRoot() : cell.getTreeItem();
                int cellIndex = cell.getIndex();
                if (cellIndex != index) {
                    if (isNotParent(dragItem, destItem)) {
                        DROPPING dropping = DROPPING.determineDropping(cell, event, BOND);
                        if (canBeChildOfParent(dragItem.getValue(), destItem.getValue()) && dropping == DROPPING.ONTO) {
                            attachTo(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateCell(cell, destItem, cellIndex);
                        } else if (dropping == DROPPING.ABOVE) {
                            putAbove(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateCell(cell, destItem, cellIndex);
                        } else if (dropping == DROPPING.BELOW) {
                            putBelow(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateCell(cell, destItem, cellIndex);
                        }
                    }
                }
            }
        });
    }

    private void initializeTree(Node placeholder) {
        tree.setSkin(new TreeViewPlaceholderSkin<>(tree, changed, placeholder,
                treeView -> Optional.ofNullable(treeView)
                        .map(TreeView::getRoot)
                        .map(TreeItem::getChildren)
                        .map(List::isEmpty)
                        .orElse(true)));
        shouldClearRoot();
        tree.setShowRoot(false);
        tree.setCellFactory(this::createDisplayItemTreeCell);
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

    public boolean isChanged() {
        return changed.getValue();
    }

    public void setChanged(boolean changed) {
        this.changed.setValue(changed);
    }

    private boolean isDifferentTrees(TreeItem<DisplayItem> oldRoot, TreeItem<DisplayItem> newRoot) {
        if (oldRoot == null) {
            return true;
        }
        if (oldRoot != newRoot) {
            DisplayItem oldRootValue = oldRoot.getValue();
            DisplayItem newRootValue = newRoot.getValue();
            if (!Objects.equals(oldRootValue, newRootValue)) {
                return true;
            }
            ObservableList<TreeItem<DisplayItem>> oldRootChildren = oldRoot.getChildren();
            ObservableList<TreeItem<DisplayItem>> newRootChildren = newRoot.getChildren();
            int size = oldRootChildren.size();
            if (size != newRootChildren.size()) {
                return true;
            }
            for (int i = 0; i < size; i++) {
                TreeItem<DisplayItem> oldChild = oldRootChildren.get(i);
                TreeItem<DisplayItem> newChild = newRootChildren.get(i);
                if (isDifferentTrees(oldChild, newChild)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTreeEmpty() {
        return tree.getRoot().getChildren().isEmpty();
    }

    private void onTemplate(ActionEvent actionEvent) {
        if (isTreeEmpty() || FXDialogs.confirm(getStage().orElse(null),
                resources.getString(LC_TEMPLATE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_TEMPLATE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_TEMPLATE_CONFIRM_CANCEL)))) {
            try {
                doTemplate();
            } catch (RuntimeException e) {
                FXDialogs.error(getStage().orElse(null), resources.getString(LC_ERROR_MALFORMED_SCRIPT), e, Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                        ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                        .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OTHER)
                        .ifPresent(buttonType -> RPyCG.mailError(e));
            }
//            updateScript(true);
        }
    }

    private void putAbove(TreeItem<DisplayItem> destItem, TreeItem<DisplayItem> dragItem) {
        ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        dragItem.getParent().getChildren().remove(dragItem);
        destSiblings.add(destSiblings.indexOf(destItem), dragItem);
        selectItem(dragItem);
    }

    private void putBelow(TreeItem<DisplayItem> destItem, TreeItem<DisplayItem> dragItem) {
        ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        int index = destSiblings.indexOf(destItem);
        dragItem.getParent().getChildren().remove(dragItem);
        if (index < destSiblings.size() - 1) {
            destSiblings.add(index + 1, dragItem);
        } else {
            destSiblings.add(dragItem);
        }
        selectItem(dragItem);
    }

    private void removeItem(TreeItem<DisplayItem> treeItem) {
        if (FXDialogs.confirm(getStage().orElse(null), resources.getString(LC_REMOVE_CONFIRM), Map.of(
                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_REMOVE_CONFIRM_OK),
                ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_REMOVE_CONFIRM_CANCEL)))) {
            treeItem.getParent().getChildren().remove(treeItem);
        }
    }

    private void replaceItem(TreeItem<DisplayItem> item, DisplayItem displayItem) {
        if (item != null) {
            item.setValue(displayItem);
            changed.setValue(true);
            selectItem(item);
        }
    }

    private boolean shouldClearRoot() {
        return shouldUpdateTree(DisplayItem.toTreeItem(DisplayItem.createRoot(), true));
    }

    private boolean shouldUpdateTree(TreeItem<DisplayItem> root) {
        if (isDifferentTrees(tree.getRoot(), root)) {
            tree.setRoot(root);
            root.setExpanded(true);
            selectItem(root);
            return true;
        }
        return false;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private void splitMenu(TreeItem<DisplayItem> item) {
        TreeItem<DisplayItem> treeItem = item == null ? tree.getRoot() : item;
        DisplayItem value = treeItem.getValue();
        if (value != null) {
            List<TreeItem<DisplayItem>> subMenus = FXCollections.observableArrayList();
            ObservableList<TreeItem<DisplayItem>> children = treeItem.getChildren();
            ObservableList<TreeItem<DisplayItem>> childrenCopy = FXCollections.observableArrayList(children);
            int childrenSize = childrenCopy.size();
            int groups = Math.max(2, childrenSize / FULLNESS.ALMOST.getSize());
            String label = value.getLabel();
            String name = value.getName();
            for (int g = 0; g < groups; g++) {
                int num = g + 1;
                subMenus.add(DisplayItem.toTreeItem(DisplayItem.createMenu
                        (String.format("%s #%d", label, num), name + '_' + num), true));
            }
            int groupSize = (int) Math.ceil(childrenSize / ((double) groups));
            for (int g = 0; g < childrenSize; g++) {
                TreeItem<DisplayItem> child = childrenCopy.get(g);
                children.remove(child);
                subMenus.get(g / groupSize).getChildren().add(child);
            }
            children.setAll(subMenus);
            changed.setValue(true);
            selectItem(treeItem);
        }
    }

    private void updateCell(TreeCell<DisplayItem> cell, TreeItem<DisplayItem> treeItem,
                            int cellIndex) {
        cell.setContextMenu(createContextMenuForCell(treeItem));
        FXRun.runLater(() -> {
            setCellZebraDecorations(cell, cell.getTreeItem(), cellIndex);
            setCellSignalDecorations(cell, cell.getTreeItem());
            setCellSplitDecorations(cell, cell.getTreeItem());
        });
    }

    private void updateTree(ModelItem root) {
        setModel(root);
    }

    private static final class DisplayItemTreeCell extends TreeCell<DisplayItem> {
        private final BiConsumer<TreeCell<DisplayItem>, DisplayItem> decorator;

        private DisplayItemTreeCell(BiConsumer<TreeCell<DisplayItem>, DisplayItem> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(DisplayItem item, boolean empty) {
            super.updateItem(item, empty);
            decorator.accept(this, empty ? null : item);
        }
    }

    private static class TreeViewPlaceholderSkin<T> extends TreeViewSkin<T> {
        private static final String CLASS_PLACEHOLDER = "placeholder";
        private final Predicate<TreeView<?>> emptyPredicate;
        private final Node placeholder;
        private final ObservableValue<?> watch;
        private StackPane placeholderRegion;

        public TreeViewPlaceholderSkin(TreeView<T> treeView, ObservableValue<?> watch,
                                       Node placeholder, Predicate<TreeView<?>> emptyPredicate) {
            super(treeView);
            this.watch = watch;
            this.placeholder = placeholder;
            this.emptyPredicate = emptyPredicate;
            installPlaceholderSupport();
        }

        private void installPlaceholderSupport() {
            registerChangeListener(watch, observable -> updatePlaceholderSupport());
            watch.addListener((observable, oldValue, newValue) -> updatePlaceholderSupport());
            updatePlaceholderSupport();
        }

        private void updatePlaceholderSupport() {
            if (isTreeEmpty()) {
                if (placeholderRegion == null) {
                    placeholderRegion = new StackPane();
                    placeholderRegion.getStyleClass().setAll(CLASS_PLACEHOLDER);
                    getChildren().add(placeholderRegion);
                    placeholderRegion.getChildren().setAll(placeholder);
                }
            }
            getVirtualFlow().setVisible(!isTreeEmpty());
            if (placeholderRegion != null) {
                placeholderRegion.setVisible(isTreeEmpty());
            }
        }

        private boolean isTreeEmpty() {
            return emptyPredicate.test(getSkinnable());
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            if (placeholderRegion != null && placeholderRegion.isVisible()) {
                placeholderRegion.resizeRelocate(x, y, w, h);
            }
        }
    }
}
