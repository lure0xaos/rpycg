package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXComponent;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.ModelTemplate;
import gargoyle.rpycg.model.ModelType;
import gargoyle.rpycg.service.ErrorMailer;
import gargoyle.rpycg.service.ModelConverter;
import gargoyle.rpycg.ui.icons.Icons;
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

    private static boolean canBeChildOfParent(final DisplayItem child, final DisplayItem parent) {
        child.getModelType();
        return ModelType.MENU == parent.getModelType();
    }

    private static FULLNESS getFullness(final TreeItem<DisplayItem> treeItem) {
        final DisplayItem item = treeItem.getValue();
        switch (item.getModelType()) {
            case VARIABLE:
                return FULLNESS.determineFullness(null == treeItem.getParent() ? 0
                        : treeItem.getParent().getChildren().size());
            case MENU:
                return FULLNESS.determineFullness(treeItem.getChildren().size());
            default:
                throw new IllegalArgumentException(String.valueOf(item.getModelType()));
        }
    }

    private static boolean isFirstChild(final TreeItem<DisplayItem> treeItem) {
        if (null == treeItem) {
            return true;
        }
        final TreeItem<DisplayItem> parent = treeItem.getParent();
        if (null == parent) {
            return true;
        }
        final ObservableList<TreeItem<DisplayItem>> children = parent.getChildren();
        return 0 == children.indexOf(treeItem);
    }

    private static boolean isLastChild(final TreeItem<DisplayItem> treeItem) {
        if (null == treeItem) {
            return false;
        }
        final TreeItem<DisplayItem> parent = treeItem.getParent();
        if (null == parent) {
            return false;
        }
        final ObservableList<TreeItem<DisplayItem>> children = parent.getChildren();
        return children.indexOf(treeItem) == children.size() - 1;
    }

    @SuppressWarnings("ObjectEquality")
    private static boolean isNotParent(final TreeItem<DisplayItem> parent, final TreeItem<DisplayItem> child) {
        TreeItem<DisplayItem> item = child;
        boolean result = true;
        while (result && null != item) {
            result = item.getParent() != parent;
            item = item.getParent();
        }
        return result;
    }

    private static void setCellSignalDecorations(final Styleable cell, final TreeItem<DisplayItem> treeItem) {
        if (null != treeItem && null != treeItem.getValue()) {
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

    private static void setCellSplitDecorations(final Styleable cell,
                                                final TreeItem<DisplayItem> treeItem) {
        Classes.classRemoveAll(cell, CLASS_VARIABLE, CLASS_MENU, CLASS_FIRST_CHILD, CLASS_LAST_CHILD);
        if (null != treeItem) {
            final DisplayItem displayItem = treeItem.getValue();
            if (null != displayItem) {
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

    private static void setCellZebraDecorations(final Styleable cell,
                                                final TreeItem<DisplayItem> treeItem, final int cellIndex) {
        Classes.classRemoveAll(cell, CLASS_ODD, CLASS_EVEN);
        if (null != treeItem && null != treeItem.getValue()) {
            if (0 == cellIndex % 2) {
                Classes.classAddRemove(cell, CLASS_EVEN, CLASS_ODD);
            } else {
                Classes.classAddRemove(cell, CLASS_ODD, CLASS_EVEN);
            }
        }
    }

    public void addRootMenu() {
        addMenu(getRoot());
    }

    public void addRootVariable() {
        addVariable(getRoot());
    }

    public SimpleBooleanProperty changedProperty() {
        return changed;
    }

    public void clearAll() {
        changed.setValue(shouldClearRoot());
    }

    public Node createPlaceHolder(final ResourceBundle resources) {
        final FXContext context = FXContextFactory.currentContext();
        final Button placeholder = new Button(resources.getString(LC_TEMPLATE),
                context.findResource(context.resolveBaseName(Icons.class, Icons.ICON_TEMPLATE), FXConstants.EXT__IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new).orElse(null));
        placeholder.setTooltip(new Tooltip(resources.getString(LC_TEMPLATE_TOOLTIP)));
        placeholder.setOnAction(this::onTemplate);
        return placeholder;
    }

    public ModelItem getModel() {
        return modelConverter.toModel(tree.getRoot());
    }

    public void setModel(final ModelItem rootItem) {
        FXRun.runLater(() -> changed.setValue(shouldUpdateTree(modelConverter.toTree(rootItem))));
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.resources = FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES,
                location.toExternalForm());
        initializeTree(createPlaceHolder(resources));
    }

    public boolean isChanged() {
        return changed.getValue();
    }

    public void setChanged(final boolean changed) {
        this.changed.setValue(changed);
    }

    public boolean isTreeEmpty() {
        return tree.getRoot().getChildren().isEmpty();
    }

    private void addItem(final TreeItem<DisplayItem> item, final DisplayItem displayItem, final boolean expanded) {
        final TreeItem<DisplayItem> treeItem = null == item ? tree.getRoot() : item;
        final TreeItem<DisplayItem> newItem = DisplayItem.toTreeItem(displayItem, expanded);
        treeItem.getChildren().add(newItem);
        changed.setValue(true);
        selectItem(newItem);
    }

    private void addMenu(final TreeItem<DisplayItem> item) {
        final MenuDialog dialog = new MenuDialog();
        dialog.setKnown(getKnownNames(""));
        getStage().ifPresent(dialog::initOwner);
        dialog.showAndWait().ifPresent(menu -> addItem(item, menu, true));
    }

    private void addVariable(final TreeItem<DisplayItem> item) {
        final VariableDialog dialog = new VariableDialog();
        getStage().ifPresent(dialog::initOwner);
        dialog.showAndWait().ifPresent(variable -> addItem(item, variable, false));
    }

    private void attachTo(final TreeItem<DisplayItem> destItem, final TreeItem<DisplayItem> dragItem) {
        dragItem.getParent().getChildren().remove(dragItem);
        destItem.getChildren().add(dragItem);
        selectItem(dragItem);
    }

    private ContextMenu createContextMenuForCell(final TreeItem<DisplayItem> treeItem) {
        final List<MenuItem> menuItems = FXCollections.observableArrayList();
        if (null != treeItem) {
            final DisplayItem displayItem = treeItem.getValue();
            if (null != displayItem) {
                final ModelType modelType = displayItem.getModelType();
                if (ModelType.VARIABLE == modelType) {
                    menuItems.add(createMenuItem(Icons.ICON_VARIABLE, "edit-variable", treeItem, this::editVariable));
                }
                if (ModelType.MENU == modelType) {
                    menuItems.add(createMenuItem(Icons.ICON_MENU, "edit-menu", treeItem, this::editMenu));
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(Icons.ICON_VARIABLE, "create-variable", treeItem, this::addVariable));
                    menuItems.add(createMenuItem(Icons.ICON_MENU, "create-menu", treeItem, this::addMenu));
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(Icons.ICON_MENU, "split-menu", treeItem, this::splitMenu));
                }
                if (null != treeItem.getParent()) {
                    menuItems.add(new SeparatorMenuItem());
                    menuItems.add(createMenuItem(Icons.ICON_DELETE, "remove", treeItem, this::removeItem));
                }
                return new ContextMenu(menuItems.toArray(MENU_ITEMS));
            }
        }
        menuItems.add(new SeparatorMenuItem());
        final TreeItem<DisplayItem> rootItem = tree.getRoot();
        menuItems.add(createMenuItem(Icons.ICON_VARIABLE, "create-variable", rootItem, this::addVariable));
        menuItems.add(createMenuItem(Icons.ICON_MENU, "create-menu", rootItem, this::addMenu));
        return new ContextMenu(menuItems.toArray(MENU_ITEMS));
    }

    private TreeCell<DisplayItem> createDisplayItemTreeCell(final TreeView<DisplayItem> treeView) {
        final TreeCell<DisplayItem> treeCell = new DisplayItemTreeCell((cell, displayItem) -> {
            cell.setText(Optional.ofNullable(displayItem)
                    .map(DisplayItem::getLabel).filter(s -> !s.isBlank()).orElseGet(() ->
                            Optional.ofNullable(displayItem).map(DisplayItem::getName).orElse("")));
            component.findResource(Optional.ofNullable(displayItem).map(DisplayItem::getModelType).map(type -> {
                        switch (type) {
                            case MENU:
                                return Icons.ICON_MENU;
                            case VARIABLE:
                                return Icons.ICON_VARIABLE;
                            default:
                                return Icons.ICON_EMPTY;
                        }
                    }).orElse(Icons.ICON_EMPTY), FXConstants.EXT__IMAGES)
                    .map(URL::toExternalForm).map(ImageView::new)
                    .ifPresent(cell::setGraphic);
            updateCell(cell, cell.getTreeItem(), cell.getIndex());
        });
        initializeDnD(treeCell);
        treeCell.indexProperty().addListener((observable, oldValue, newValue) ->
                updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
        treeCell.treeViewProperty().addListener((observable, oldValue, newValue) -> {
            if (null != newValue) {
                newValue.setOnScroll(scrollEvent ->
                        updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
            }
        });
        treeCell.hoverProperty().addListener((observable, oldValue, newValue) ->
                updateCell(treeCell, treeCell.getTreeItem(), treeCell.getIndex()));
        return treeCell;
    }

    private MenuItem createMenuItem(final String graphicName,
                                    final String key,
                                    final TreeItem<DisplayItem> treeItem,
                                    final Consumer<? super TreeItem<DisplayItem>> handler) {
        final MenuItem item = new MenuItem(resources.getString(key), Optional.ofNullable(graphicName)
                .flatMap(location ->
                        FXContextFactory.currentContext().findResource(Icons.class, location, FXConstants.EXT__IMAGES)
                                .map(URL::toExternalForm).map(ImageView::new))
                .orElse(null));
        item.setOnAction(event -> handler.accept(treeItem));
        return item;
    }

    private void doTemplate() {
        Optional.ofNullable(FXContextFactory.currentContext().getParameters())
                .map(Application.Parameters::getNamed)
                .map(map -> map.get(KEY_DEBUG))
                .ifPresentOrElse((s) -> updateTree(ModelTemplate.getTestTemplateTree()),
                        () -> updateTree(ModelTemplate.getTemplateTree()));
    }

    private void editMenu(final TreeItem<DisplayItem> item) {
        final MenuDialog dialog = new MenuDialog();
        dialog.setKnown(getKnownNames(null == item || null == item.getValue() ? "" : item.getValue().getName()));
        getStage().ifPresent(dialog::initOwner);
        if (null != item) {
            dialog.setDisplayItem(item.getValue());
        }
        dialog.showAndWait().ifPresent(menu -> replaceItem(item, menu));
    }

    private void editVariable(final TreeItem<DisplayItem> item) {
        final VariableDialog dialog = new VariableDialog();
        getStage().ifPresent(dialog::initOwner);
        dialog.setDisplayItem(item.getValue());
        dialog.showAndWait().ifPresent(variable -> replaceItem(item, variable));
    }

    private Set<String> getKnownNames(final String allow) {
        final Set<String> known = new HashSet<>(tree.getChildrenUnmodifiable().size());
        TreeItemWalker.visitItems(tree, displayItem -> {
            final String name = displayItem.getName();
            if (!name.equalsIgnoreCase(allow)) {
                known.add(name);
            }
        });
        return known;
    }

    private TreeItem<DisplayItem> getRoot() {
        return tree.getRoot();
    }

    private Optional<Stage> getStage() {
        return FXUtil.findStage(component.getView());
    }

    private void initializeDnD(final TreeCell<DisplayItem> cell) {
        cell.setOnDragDetected(event -> {
            if (!cell.isEmpty()) {
                final Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(cell.snapshot(null, null));
                final Map<DataFormat, Object> content = new ClipboardContent();
                content.put(SERIALIZED_MIME_TYPE, cell.getIndex());
                dragboard.setContent(content);
                event.consume();
            }
        });
        cell.setOnDragOver(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                final TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                final int index = tree.getRow(dragItem);
                final TreeItem<DisplayItem> destItem = cell.isEmpty() ? tree.getRoot() : cell.getTreeItem();
                if (cell.getIndex() != index) {
                    if (isNotParent(dragItem, destItem)) {
                        final DROPPING dropping = DROPPING.determineDropping(cell, event, BOND);
                        if (canBeChildOfParent(dragItem.getValue(), destItem.getValue()) && DROPPING.ONTO == dropping) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        } else if (DROPPING.ABOVE == dropping || DROPPING.BELOW == dropping) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        }
                    }
                }
            }
        });
        cell.setOnDragDropped(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                final TreeItem<DisplayItem> dragItem = tree.getTreeItem((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE));
                final int index = tree.getRow(dragItem);
                final TreeItem<DisplayItem> destItem = cell.isEmpty() ? tree.getRoot() : cell.getTreeItem();
                final int cellIndex = cell.getIndex();
                if (cellIndex != index) {
                    if (isNotParent(dragItem, destItem)) {
                        final DROPPING dropping = DROPPING.determineDropping(cell, event, BOND);
                        if (canBeChildOfParent(dragItem.getValue(), destItem.getValue()) && DROPPING.ONTO == dropping) {
                            attachTo(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateCell(cell, destItem, cellIndex);
                        } else if (DROPPING.ABOVE == dropping) {
                            putAbove(destItem, dragItem);
                            event.setDropCompleted(true);
                            event.consume();
                            updateCell(cell, destItem, cellIndex);
                        } else if (DROPPING.BELOW == dropping) {
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

    private void initializeTree(final Node placeholder) {
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
            if (0 < event.getY()) {
                scrollDirection = 1.0 / tree.getExpandedItemCount();
            } else {
                scrollDirection = -1.0 / tree.getExpandedItemCount();
            }
            scrollTimeline.play();
        });
        tree.setOnDragEntered(event -> scrollTimeline.stop());
        tree.setOnDragDone(event -> scrollTimeline.stop());
    }

    private boolean isDifferentTrees(final TreeItem<DisplayItem> oldRoot, final TreeItem<DisplayItem> newRoot) {
        if (null == oldRoot) {
            return true;
        }
        if (oldRoot != newRoot) {
            final DisplayItem oldRootValue = oldRoot.getValue();
            final DisplayItem newRootValue = newRoot.getValue();
            if (!Objects.equals(oldRootValue, newRootValue)) {
                return true;
            }
            final ObservableList<TreeItem<DisplayItem>> oldRootChildren = oldRoot.getChildren();
            final ObservableList<TreeItem<DisplayItem>> newRootChildren = newRoot.getChildren();
            final int size = oldRootChildren.size();
            if (size != newRootChildren.size()) {
                return true;
            }
            for (int i = 0; i < size; i++) {
                final TreeItem<DisplayItem> oldChild = oldRootChildren.get(i);
                final TreeItem<DisplayItem> newChild = newRootChildren.get(i);
                if (isDifferentTrees(oldChild, newChild)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onTemplate(final ActionEvent actionEvent) {
        final FXContext context = FXContextFactory.currentContext();
        if (isTreeEmpty() || context.confirm(getStage().orElse(null),
                resources.getString(LC_TEMPLATE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_TEMPLATE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_TEMPLATE_CONFIRM_CANCEL)))) {
            try {
                doTemplate();
            } catch (final RuntimeException e) {
                context.error(getStage().orElse(null), resources.getString(LC_ERROR_MALFORMED_SCRIPT),
                                e, Map.of(
                                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_CLOSE),
                                        ButtonBar.ButtonData.OTHER, resources.getString(LC_REPORT)))
                        .filter(buttonType -> ButtonBar.ButtonData.OTHER == buttonType.getButtonData())
                        .ifPresent(buttonType -> ErrorMailer.mailError(e));
            }
//            updateScript(true);
        }
    }

    private void putAbove(final TreeItem<DisplayItem> destItem, final TreeItem<DisplayItem> dragItem) {
        final ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        dragItem.getParent().getChildren().remove(dragItem);
        destSiblings.add(destSiblings.indexOf(destItem), dragItem);
        selectItem(dragItem);
    }

    private void putBelow(final TreeItem<DisplayItem> destItem, final TreeItem<DisplayItem> dragItem) {
        final ObservableList<TreeItem<DisplayItem>> destSiblings = destItem.getParent().getChildren();
        final int index = destSiblings.indexOf(destItem);
        dragItem.getParent().getChildren().remove(dragItem);
        if (index < destSiblings.size() - 1) {
            destSiblings.add(index + 1, dragItem);
        } else {
            destSiblings.add(dragItem);
        }
        selectItem(dragItem);
    }

    private void removeItem(final TreeItem<DisplayItem> treeItem) {
        if (FXContextFactory.currentContext().confirm(getStage().orElse(null),
                resources.getString(LC_REMOVE_CONFIRM), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_REMOVE_CONFIRM_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_REMOVE_CONFIRM_CANCEL)))) {
            treeItem.getParent().getChildren().remove(treeItem);
        }
    }

    private void replaceItem(final TreeItem<DisplayItem> item, final DisplayItem displayItem) {
        if (null != item) {
            item.setValue(displayItem);
            changed.setValue(true);
            selectItem(item);
        }
    }

    private void selectItem(final TreeItem<DisplayItem> item) {
        TreeItem<DisplayItem> treeItem = item;
        while (null != treeItem) {
            treeItem.setExpanded(null != treeItem.getValue() && ModelType.MENU == treeItem.getValue().getModelType());
            treeItem = treeItem.getParent();
        }
        tree.getSelectionModel().select(item);
        tree.scrollTo(tree.getRow(item));
    }

    private boolean shouldClearRoot() {
        return shouldUpdateTree(DisplayItem.toTreeItem(DisplayItem.createRoot(), true));
    }

    private boolean shouldUpdateTree(final TreeItem<DisplayItem> root) {
        if (isDifferentTrees(tree.getRoot(), root)) {
            tree.setRoot(root);
            root.setExpanded(true);
            selectItem(root);
            return true;
        }
        return false;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private void splitMenu(final TreeItem<DisplayItem> item) {
        final TreeItem<DisplayItem> treeItem = null == item ? tree.getRoot() : item;
        final DisplayItem value = treeItem.getValue();
        if (null != value) {
            final List<TreeItem<DisplayItem>> subMenus = FXCollections.observableArrayList();
            final ObservableList<TreeItem<DisplayItem>> children = treeItem.getChildren();
            final ObservableList<TreeItem<DisplayItem>> childrenCopy = FXCollections.observableArrayList(children);
            final int childrenSize = childrenCopy.size();
            final int groups = Math.max(2, childrenSize / FULLNESS.ALMOST.getSize());
            final String label = value.getLabel();
            final String name = value.getName();
            for (int g = 0; g < groups; g++) {
                final int num = g + 1;
                subMenus.add(DisplayItem.toTreeItem(DisplayItem.createMenu
                        (String.format("%s #%d", label, num), name + '_' + num), true));
            }
            final int groupSize = (int) Math.ceil(childrenSize / ((double) groups));
            for (int g = 0; g < childrenSize; g++) {
                final TreeItem<DisplayItem> child = childrenCopy.get(g);
                children.remove(child);
                subMenus.get(g / groupSize).getChildren().add(child);
            }
            children.setAll(subMenus);
            changed.setValue(true);
            selectItem(treeItem);
        }
    }

    private void updateCell(final TreeCell<DisplayItem> cell, final TreeItem<DisplayItem> treeItem,
                            final int cellIndex) {
        cell.setContextMenu(createContextMenuForCell(treeItem));
        FXRun.runLater(() -> {
            setCellZebraDecorations(cell, cell.getTreeItem(), cellIndex);
            setCellSignalDecorations(cell, cell.getTreeItem());
            setCellSplitDecorations(cell, cell.getTreeItem());
        });
    }

    private void updateTree(final ModelItem root) {
        setModel(root);
    }

    private static final class DisplayItemTreeCell extends TreeCell<DisplayItem> {
        private final BiConsumer<? super TreeCell<DisplayItem>, ? super DisplayItem> decorator;

        private DisplayItemTreeCell(final BiConsumer<? super TreeCell<DisplayItem>, ? super DisplayItem> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(final DisplayItem item, final boolean empty) {
            super.updateItem(item, empty);
            decorator.accept(this, empty ? null : item);
        }
    }

    private static final class TreeViewPlaceholderSkin<T> extends TreeViewSkin<T> {
        private static final String CLASS_PLACEHOLDER = "placeholder";
        private final Predicate<? super TreeView<?>> emptyPredicate;
        private final Node placeholder;
        private final ObservableValue<?> watch;
        private StackPane placeholderRegion;

        public TreeViewPlaceholderSkin(final TreeView<T> treeView, final ObservableValue<?> watch,
                                       final Node placeholder, final Predicate<? super TreeView<?>> emptyPredicate) {
            super(treeView);
            this.watch = watch;
            this.placeholder = placeholder;
            this.emptyPredicate = emptyPredicate;
            installPlaceholderSupport();
        }

        @Override
        protected void layoutChildren(final double x, final double y, final double w, final double h) {
            super.layoutChildren(x, y, w, h);
            if (null != placeholderRegion && placeholderRegion.isVisible()) {
                placeholderRegion.resizeRelocate(x, y, w, h);
            }
        }

        private void installPlaceholderSupport() {
            registerChangeListener(watch, observable -> updatePlaceholderSupport());
            watch.addListener((observable, oldValue, newValue) -> updatePlaceholderSupport());
            updatePlaceholderSupport();
        }

        private boolean isTreeEmpty() {
            return emptyPredicate.test(getSkinnable());
        }

        private void updatePlaceholderSupport() {
            if (isTreeEmpty()) {
                if (null == placeholderRegion) {
                    placeholderRegion = new StackPane();
                    placeholderRegion.getStyleClass().setAll(CLASS_PLACEHOLDER);
                    getChildren().add(placeholderRegion);
                    placeholderRegion.getChildren().setAll(placeholder);
                }
            }
            getVirtualFlow().setVisible(!isTreeEmpty());
            if (null != placeholderRegion) {
                placeholderRegion.setVisible(isTreeEmpty());
            }
        }
    }
}
