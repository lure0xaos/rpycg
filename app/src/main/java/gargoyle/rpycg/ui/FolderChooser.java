package gargoyle.rpycg.ui;

import gargoyle.fx.FXComponent;
import gargoyle.fx.FXContextFactory;
import gargoyle.fx.FXRun;
import gargoyle.fx.FXUtil;
import gargoyle.fx.log.FXLog;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.ui.icons.Icon;
import gargoyle.rpycg.util.GameUtil;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FolderChooser extends Dialog<Path> implements Initializable {
    private static final String LC_CANCEL = "cancel";
    private static final String LC_OK = "ok";
    private static final String LC_ROOT = "root";
    private static final String LC_TITLE = "title";
    private final Property<BiFunction<Path, Boolean, Optional<Node>>> additionalIconProvider;
    private final FXComponent<FolderChooser, Parent> component;
    private final FileWatcher fileWatcher;
    private final Property<Path> initialDirectory;
    private final Property<Predicate<Path>> selectionFilter;
    @FXML
    private TreeView<Path> fileTree;
    private String rootLabel;

    public FolderChooser() {
        fileWatcher = new FileWatcher();
        initialDirectory = new SimpleObjectProperty<>(null);
        selectionFilter = new SimpleObjectProperty<>(null);
        additionalIconProvider = new SimpleObjectProperty<>(null);
        component = FXContextFactory.currentContext().loadDialog(this)
                .orElseThrow(() ->
                        new AppUserException("No view {resource}", Map.of("resource", FolderChooser.class.getName())));
    }

    private static ObservableList<TreeItem<Path>> findChildren(final Path path, final FileWatcher fileWatcher,
                                                               final BiFunction<Path, Boolean, Optional<Node>>
                                                                       iconProvider,
                                                               final BiConsumer<TreeItem<Path>, Boolean> navigate) {
        if (null == path) {
            return FXCollections.observableArrayList(
                    StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)
                            .filter(Files::isDirectory).sorted()
                            .map(watchPath -> new FileTreeItem(watchPath, fileWatcher, iconProvider, navigate))
                            .collect(Collectors.toList()));
        }
        if (Files.isDirectory(path)) {
            try (final Stream<Path> list = Files.list(path)) {
                final List<FileTreeItem> collect = list.filter(Files::isDirectory).sorted()
                        .map(watchPath -> new FileTreeItem(watchPath, fileWatcher, iconProvider, navigate))
                        .collect(Collectors.toList());
                return FXCollections.observableArrayList(collect);
            } catch (final IOException e) {
                return FXCollections.observableArrayList();
            }
        }
        return FXCollections.observableArrayList();
    }

    private static String getPathText(final Path path, final boolean full, final String rootLabel) {
        if (null != path) {
            if (full || null == path.getFileName()) {
                try {
                    if (Files.isSameFile(path, path.getRoot())) {
                        return FileSystemView.getFileSystemView().getSystemDisplayName(path.toFile());
                    }
                } catch (final IOException e) {
                    return path.toString();
                }
                return path.toString();
            }
            return path.getFileName().toString();
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException x) {
            return rootLabel;
        }
    }

    public Property<BiFunction<Path, Boolean, Optional<Node>>> additionalIconProviderProperty() {
        return additionalIconProvider;
    }

    public void dispose() {
        fileWatcher.close();
    }

    public BiFunction<Path, Boolean, Optional<Node>> getAdditionalIconProvider() {
        return additionalIconProvider.getValue();
    }

    public void setAdditionalIconProvider(final BiFunction<Path, Boolean, Optional<Node>> additionalIconProvider) {
        this.additionalIconProvider.setValue(additionalIconProvider);
    }

    public Path getInitialDirectory() {
        return initialDirectory.getValue();
    }

    public void setInitialDirectory(final Path initialDirectory) {
        this.initialDirectory.setValue(initialDirectory);
    }

    public Predicate<Path> getSelectionFilter() {
        return selectionFilter.getValue();
    }

    public void setSelectionFilter(final Predicate<Path> selectionFilter) {
        this.selectionFilter.setValue(selectionFilter);
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        FXUtil.requireNonNull(resources, "No resources {location}",
                Map.of("location", location.toExternalForm()));
        FXContextFactory.currentContext().decorateDialog(this, buttonType ->
                buttonType.getButtonData().isCancelButton() ? null :
                        fileTree.getSelectionModel().getSelectedItem().getValue(), Map.of(
                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_OK),
                ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CANCEL)
        ), resources.getString(LC_TITLE));
        setResizable(true);
        rootLabel = resources.getString(LC_ROOT);
        fileTree.setCellFactory(treeView -> new FileTreeCell(rootLabel, this::getPathGraphic));
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        final TreeItem<Path> rootNode = new TreeItem<>(null, getPathGraphic(null, false)
                .orElse(null));
        rootNode.getChildren().setAll(findChildren(null, fileWatcher, this::getPathGraphic,
                (treeItem, expanded) -> scrollTo(treeItem)));
        rootNode.setExpanded(true);
        fileTree.setRoot(rootNode);
        fileTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final Predicate<Path> selectionFilterValue = selectionFilter.getValue();
            if (null != selectionFilterValue && null != newValue && null != newValue.getValue()) {
                getDialogPane().getButtonTypes().stream()
                        .filter(buttonType -> buttonType.getButtonData().isDefaultButton())
                        .findFirst().ifPresent(buttonType -> getDialogPane().lookupButton(buttonType)
                                .setDisable(!selectionFilterValue.test(newValue.getValue())));
            }
        });
        initialDirectory.addListener((observable, oldValue, newValue) -> {
            if (null != newValue) {
                selectItem(newValue, !GameUtil.isGameDirectory(newValue));
            }
        });
        FXRun.runLater(() -> fileTree.requestFocus());
    }

    public Property<Predicate<Path>> selectionFilterProperty() {
        return selectionFilter;
    }

    public Path showDialog() {
        fileWatcher.start();
        final Stage stage = component.getPrimaryStage();
        if (!stage.isShowing()) {
            initOwner(stage);
        }
        updateItems(fileTree.getSelectionModel().getSelectedItem().getValue());
        final Optional<Path> file = showAndWait();
        fileWatcher.close();
        file.ifPresent(initialDirectory::setValue);
        return file.orElse(null);
    }

    @Override
    public String toString() {
        return getPathText(null, false, rootLabel);
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    private Path getExistingParent(final Path curPath) {
        Path path = curPath;
        while (!Files.isReadable(path)) {
            path = path.getParent();
        }
        return path;
    }

    private TreeItem<Path> getItems(final TreeItem<Path> root,
                                    final Collection<? super TreeItem<Path>> result,
                                    final Path targetPath) {
        TreeItem<Path> item = root;
        for (final Path path : getParentPaths(targetPath)) {
            for (final TreeItem<Path> child : item.getChildren()) {
                if (path.equals(child.getValue())) {
                    item = child;
                    result.add(item);
                }
            }
        }
        return item;
    }

    private List<Path> getParentPaths(final Path curPath) {
        Path path = curPath;
        final List<Path> paths = new ArrayList<>(path.getNameCount());
        while (null != path) {
            paths.add(0, path);
            path = path.getParent();
        }
        return paths;
    }

    private Optional<Node> getPathGraphic(final Path path, final boolean expanded) {
        return Optional.ofNullable(additionalIconProvider.getValue())
                .map(iconProvider -> iconProvider.apply(path, expanded))
                .filter(Optional::isPresent)
                .orElseGet(() -> (null == path ? Icon.COMPUTER :
                        Files.isDirectory(path) ?
                                expanded ? Icon.FOLDER_OPEN : Icon.FOLDER :
                                Icon.FILE)
                        .findIcon(FXContextFactory.currentContext())
                        .map(URL::toExternalForm)
                        .map(ImageView::new));
    }

    private void scrollTo(final TreeItem<Path> item) {
        fileTree.scrollTo(fileTree.getRow(item));
    }

    private void selectItem(final Path path, final boolean expanded) {
        final Collection<TreeItem<Path>> result = new LinkedList<>();
        final TreeItem<Path> item = getItems(fileTree.getRoot(), result, getExistingParent(path));
        result.forEach(treeItem -> {
            treeItem.setExpanded(true);
            fileTree.getSelectionModel().select(treeItem);
        });
        item.setExpanded(expanded);
        scrollTo(item);
    }

    private void updateItems(final Path path) {
        final Collection<TreeItem<Path>> result = new LinkedList<>();
        getItems(fileTree.getRoot(), result, path);
        result.forEach(treeItem -> {
            final ObservableList<TreeItem<Path>> oldChildren = treeItem.getChildren();
            final ObservableList<TreeItem<Path>> newChildren = FXCollections.observableArrayList(oldChildren);
            newChildren.removeIf(item -> !Files.isReadable(item.getValue()));
            FolderChooser.findChildren(treeItem.getValue(), fileWatcher, this::getPathGraphic,
                            (item, expanded) -> scrollTo(item))
                    .stream().filter(item -> Files.isReadable(item.getValue())
                            && !oldChildren.contains(item))
                    .forEach(newChildren::add);
            oldChildren.setAll(newChildren);
        });
    }

    private static final class FileTreeCell extends TreeCell<Path> {
        private final BiFunction<? super Path, ? super Boolean, Optional<Node>> iconProvider;
        private final String rootLabel;

        private FileTreeCell(final String rootLabel,
                             final BiFunction<? super Path, ? super Boolean, Optional<Node>> iconProvider) {
            this.rootLabel = rootLabel;
            this.iconProvider = iconProvider;
        }

        @Override
        protected void updateItem(final Path item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || null == getTreeItem()) {
                setGraphic(null);
            } else {
                iconProvider.apply(item, getTreeItem().isExpanded()).ifPresent(this::setGraphic);
            }
            setText(empty ? null : getPathText(item, false, rootLabel));
            setTooltip(empty ? null : new Tooltip(getPathText(item, true, rootLabel)));
        }
    }

    private static final class FileTreeItem extends TreeItem<Path> {
        private final FileWatcher fileWatcher;
        private final BiFunction<Path, Boolean, Optional<Node>> iconProvider;
        private final BiConsumer<TreeItem<Path>, Boolean> navigate;
        private boolean fetch;

        private FileTreeItem(final Path path, final FileWatcher fileWatcher,
                             final BiFunction<Path, Boolean, Optional<Node>> iconProvider,
                             final BiConsumer<TreeItem<Path>, Boolean> navigate) {
            super(path);
            this.fileWatcher = fileWatcher;
            this.iconProvider = iconProvider;
            this.navigate = navigate;
            if (null != path) {
                setValue(path);
                if (Files.isDirectory(path)) {
                    addEventHandler(TreeItem.branchCollapsedEvent(), (TreeModificationEvent<Path> e) -> {
                        final TreeItem<Path> treeItem = e.getSource();
                        iconProvider.apply(treeItem.getValue(), e.getSource().isExpanded())
                                .ifPresent(treeItem::setGraphic);
                        treeItem.getChildren().clear();
                        FXRun.runLater(() -> navigate.accept(treeItem, false));
                    });
                    addEventHandler(TreeItem.branchExpandedEvent(), (TreeModificationEvent<Path> e) -> {
                        final TreeItem<Path> treeItem = e.getSource();
                        updateChildren(treeItem);
                        iconProvider.apply(treeItem.getValue(), treeItem.isExpanded())
                                .ifPresent(treeItem::setGraphic);
                        fetch = false;
                        FXRun.runLater(() -> navigate.accept(treeItem, true));
                    });
                    fileWatcher.register(getValue(), ev -> updateChildren(this));
                }
                iconProvider.apply(path, false).ifPresent(this::setGraphic);
                parentProperty().addListener((observable, oldValue, newValue) -> {
                    if (null == newValue) {
                        fetch = false;
                        fileWatcher.unregister(path);
                    }
                });
            }
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof TreeItem && getValue().equals(((TreeItem<Path>) obj).getValue());
        }

        @Override
        public boolean isLeaf() {
            return Files.isRegularFile(getValue());
        }

        @Override
        public ObservableList<TreeItem<Path>> getChildren() {
            final ObservableList<TreeItem<Path>> oldChildren = super.getChildren();
            if (fetch) {
                return oldChildren;
            }
            fetch = true;
            final ObservableList<TreeItem<Path>> newChildren = findNewChildren(this, oldChildren, navigate);
            oldChildren.setAll(newChildren);
            return newChildren;
        }

        @SuppressWarnings("TypeMayBeWeakened")
        private ObservableList<TreeItem<Path>> findNewChildren(final TreeItem<? extends Path> treeItem, final ObservableList<? extends TreeItem<Path>> oldChildren,
                                                               final BiConsumer<TreeItem<Path>, Boolean> collapse) {
            final ObservableList<TreeItem<Path>> newChildren = FXCollections.observableArrayList(oldChildren);
            newChildren.removeIf(item -> !Files.isReadable(item.getValue()));
            FolderChooser.findChildren(treeItem.getValue(), fileWatcher, iconProvider, collapse)
                    .stream().filter(item -> Files.isReadable(item.getValue())
                            && !oldChildren.contains(item)
                            && item.getValue().getParent().equals(treeItem.getValue()))
                    .forEach(newChildren::add);
            return newChildren;
        }

        private Boolean updateChildren(final TreeItem<Path> treeItem) {
            final ObservableList<TreeItem<Path>> oldChildren = treeItem.getChildren();
            fetch = true;
            final ObservableList<TreeItem<Path>> newChildren = findNewChildren(treeItem, oldChildren, navigate);
            oldChildren.setAll(newChildren);
            return fetch;
        }
    }

    private static final class FileWatcher implements Closeable {
        private final Map<Path, FileWatcherData> watch = new ConcurrentHashMap<>(2);
        private final WatchService watchService;
        private volatile boolean running;

        public FileWatcher() {
            try {
                watchService = FileSystems.getDefault().newWatchService();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            running = false;
            try {
                watch.forEach((key, value) -> unregister(key));
                watch.clear();
                watchService.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings({"BusyWait", "NestedAssignment", "MethodCallInLoopCondition"})
        private Thread createThread() {
            return new Thread(() -> {
                running = true;
                while (running) {
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        FXLog.error(e, "interrupted");
                    }
                    final Iterator<Map.Entry<Path, FileWatcherData>> iterator = watch.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<Path, FileWatcherData> entry = iterator.next();
                        final Path path = entry.getKey();
                        final FileWatcherData data = entry.getValue();
                        if (null != data) {
                            final Path dataPath = data.getPath();
                            try {
                                WatchKey key;
                                final List<FileWatcherEventData> watchPaths = new LinkedList<>();
                                while (null != (key = watchService.poll())) {
                                    for (final WatchEvent<?> watchEvent : key.pollEvents()) {
                                        if (null == path || dataPath.equals(path)) {
                                            watchPaths.add(new FileWatcherEventData(
                                                    dataPath.resolve((Path) watchEvent.context()),
                                                    watchEvent.kind(),
                                                    watchEvent.count()));
                                        }
                                    }
                                    key.reset();
                                }
                                if (!watchPaths.isEmpty() && null != path) {
                                    data.getCallback().call(new FileWatcherEvent(path, watchPaths));
                                }
                            } catch (final ClosedWatchServiceException e) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }, FileWatcher.class.getName());
        }

        private void register(final Path path, final Callback<FileWatcherEvent, Boolean> callback) {
            if (null != path && Files.isReadable(path) && !watch.containsKey(path)) {
                try {
                    path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    watch.put(path, new FileWatcherData(path, callback));
                } catch (final IOException e) {
                    FXLog.error(e, "register");
                }
            }
        }

        private void start() {
            final Thread thread = createThread();
            thread.setDaemon(true);
            thread.start();
        }

        private void unregister(final Path path) {
            watch.remove(path);
        }
    }

    private static final class FileWatcherData {
        private final Callback<FileWatcherEvent, Boolean> callback;
        private final Path path;

        private FileWatcherData(final Path path, final Callback<FileWatcherEvent, Boolean> callback) {
            this.path = path;
            this.callback = callback;
        }

        private Callback<FileWatcherEvent, Boolean> getCallback() {
            return callback;
        }

        private Path getPath() {
            return path;
        }
    }

    private static final class FileWatcherEvent {
        private final Path registeredPath;
        private final List<FileWatcherEventData> watcherEventData;

        private FileWatcherEvent(final Path registeredPath, final List<FileWatcherEventData> watcherEventData) {
            this.registeredPath = registeredPath;
            this.watcherEventData = watcherEventData;
        }

        private Path getRegisteredPath() {
            return registeredPath;
        }

        private List<FileWatcherEventData> getWatcherEventData() {
            return watcherEventData;
        }
    }

    private static final class FileWatcherEventData {
        private final int count;
        private final WatchEvent.Kind<?> kind;
        private final Path path;

        private FileWatcherEventData(final Path path, final WatchEvent.Kind<?> kind, final int count) {
            this.path = path;
            this.kind = kind;
            this.count = count;
        }

        private int getCount() {
            return count;
        }

        private WatchEvent.Kind<?> getKind() {
            return kind;
        }

        private Path getPath() {
            return path;
        }
    }
}
