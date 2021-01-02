package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.fx.Logger;
import gargoyle.rpycg.fx.LoggerFactory;
import gargoyle.rpycg.util.GameUtil;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
    private static final String ICON_COMPUTER = "icons/computer";
    private static final String ICON_FILE = "icons/text-x-generic";
    private static final String ICON_FOLDER = "icons/folder";
    private static final String ICON_FOLDER_OPEN = "icons/folder-open";
    private static final String LC_CANCEL = "cancel";
    private static final String LC_OK = "ok";
    private static final String LC_ROOT = "root";
    private static final String LC_TITLE = "title";
    private final Property<BiFunction<Path, Boolean, Optional<Node>>> additionalIconProvider;
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
        FXContextFactory.currentContext().loadDialog(this)
                .orElseThrow(() ->
                        new AppUserException(AppUserException.LC_ERROR_NO_VIEW, FolderChooser.class.getName()));
    }

    public Property<BiFunction<Path, Boolean, Optional<Node>>> additionalIconProviderProperty() {
        return additionalIconProvider;
    }

    public BiFunction<Path, Boolean, Optional<Node>> getAdditionalIconProvider() {
        return additionalIconProvider.getValue();
    }

    public void setAdditionalIconProvider(BiFunction<Path, Boolean, Optional<Node>> additionalIconProvider) {
        this.additionalIconProvider.setValue(additionalIconProvider);
    }

    public Path getInitialDirectory() {
        return initialDirectory.getValue();
    }

    public void setInitialDirectory(Path initialDirectory) {
        this.initialDirectory.setValue(initialDirectory);
    }

    public Predicate<Path> getSelectionFilter() {
        return selectionFilter.getValue();
    }

    public void setSelectionFilter(Predicate<Path> selectionFilter) {
        this.selectionFilter.setValue(selectionFilter);
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES, location.toExternalForm());
        FXDialogs.decorateDialog(this, buttonType ->
                buttonType.getButtonData().isCancelButton() ? null :
                        fileTree.getSelectionModel().getSelectedItem().getValue(), Map.of(
                ButtonBar.ButtonData.OK_DONE, resources.getString(LC_OK),
                ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CANCEL)
        ), resources.getString(LC_TITLE));
        setResizable(true);
        rootLabel = resources.getString(LC_ROOT);
        fileTree.setCellFactory(treeView -> new FileTreeCell(rootLabel, this::getPathGraphic));
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TreeItem<Path> rootNode = new TreeItem<>(null, getPathGraphic(null, false)
                .orElse(null));
        rootNode.getChildren().setAll(findChildren(null, fileWatcher, this::getPathGraphic,
                (treeItem, expanded) -> scrollTo(treeItem)));
        rootNode.setExpanded(true);
        fileTree.setRoot(rootNode);
        fileTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<Path> selectionFilterValue = selectionFilter.getValue();
            if (selectionFilterValue != null && newValue != null && newValue.getValue() != null) {
                getDialogPane().getButtonTypes().stream()
                        .filter(buttonType -> buttonType.getButtonData().isDefaultButton())
                        .findFirst().ifPresent(buttonType -> getDialogPane().lookupButton(buttonType)
                        .setDisable(!selectionFilterValue.test(newValue.getValue())));
            }
        });
        initialDirectory.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectItem(newValue, !GameUtil.isGameDirectory(newValue));
            }
        });
        FXRun.runLater(() -> fileTree.requestFocus());
    }

    private Optional<Node> getPathGraphic(Path path, boolean expanded) {
        FXContext context = FXContextFactory.currentContext();
        return Optional.ofNullable(additionalIconProvider.getValue())
                .map(iconProvider -> iconProvider.apply(path, expanded))
                .filter(Optional::isPresent)
                .orElseGet(() -> {
                    String baseName = path == null ? ICON_COMPUTER :
                            Files.isDirectory(path) ?
                                    expanded ? ICON_FOLDER_OPEN : ICON_FOLDER :
                                    ICON_FILE;
                    return context.findResource(
                            context.getBaseName(FolderChooser.class, baseName),
                            FXConstants.EXT_IMAGES)
                            .map(URL::toExternalForm)
                            .map(ImageView::new);
                });
    }

    private static ObservableList<TreeItem<Path>> findChildren(Path path, FileWatcher fileWatcher,
                                                               BiFunction<Path, Boolean, Optional<Node>>
                                                                       iconProvider,
                                                               BiConsumer<TreeItem<Path>, Boolean> navigate) {
        if (path == null) {
            return FXCollections.observableArrayList(
                    StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)
                            .filter(Files::isDirectory).sorted()
                            .map(watchPath -> new FileTreeItem(watchPath, fileWatcher, iconProvider, navigate))
                            .collect(Collectors.toList()));
        }
        if (Files.isDirectory(path)) {
            try (Stream<Path> list = Files.list(path)) {
                return FXCollections.observableArrayList(list.filter(Files::isDirectory).sorted()
                        .map(watchPath -> new FileTreeItem(watchPath, fileWatcher, iconProvider, navigate))
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                return FXCollections.observableArrayList();
            }
        }
        return FXCollections.observableArrayList();
    }

    private void scrollTo(TreeItem<Path> item) {
        fileTree.scrollTo(fileTree.getRow(item));
    }

    private void selectItem(Path path, boolean expanded) {
        Collection<TreeItem<Path>> result = new LinkedList<>();
        TreeItem<Path> item = getItems(fileTree.getRoot(), result, getExistingParent(path));
        result.forEach(treeItem -> {
            treeItem.setExpanded(true);
            fileTree.getSelectionModel().select(treeItem);
        });
        item.setExpanded(expanded);
        scrollTo(item);
    }

    private TreeItem<Path> getItems(TreeItem<Path> root,
                                    Collection<TreeItem<Path>> result,
                                    Path targetPath) {
        TreeItem<Path> item = root;
        for (Path path : getParentPaths(targetPath)) {
            for (TreeItem<Path> child : item.getChildren()) {
                if (path.equals(child.getValue())) {
                    item = child;
                    result.add(item);
                }
            }
        }
        return item;
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    private Path getExistingParent(Path curPath) {
        Path path = curPath;
        while (!Files.isReadable(path)) {
            path = path.getParent();
        }
        return path;
    }

    private List<Path> getParentPaths(Path curPath) {
        Path path = curPath;
        List<Path> paths = new ArrayList<>(path.getNameCount());
        while (path != null) {
            paths.add(0, path);
            path = path.getParent();
        }
        return paths;
    }

    public Property<Predicate<Path>> selectionFilterProperty() {
        return selectionFilter;
    }

    public Path showDialog(Stage stage) {
        fileWatcher.start();
        if (!stage.isShowing()) {
            initOwner(stage);
        }
        updateItems(fileTree.getSelectionModel().getSelectedItem().getValue());
        Optional<Path> file = showAndWait();
        fileWatcher.close();
        file.ifPresent(initialDirectory::setValue);
        return file.orElse(null);
    }

    private void updateItems(Path path) {
        Collection<TreeItem<Path>> result = new LinkedList<>();
        getItems(fileTree.getRoot(), result, path);
        result.forEach(treeItem -> {
            ObservableList<TreeItem<Path>> oldChildren = treeItem.getChildren();
            ObservableList<TreeItem<Path>> newChildren = FXCollections.observableArrayList(oldChildren);
            newChildren.removeIf(item -> !Files.isReadable(item.getValue()));
            FolderChooser.findChildren(treeItem.getValue(), fileWatcher, this::getPathGraphic,
                    (item, expanded) -> scrollTo(item))
                    .stream().filter(item -> Files.isReadable(item.getValue())
                    && !oldChildren.contains(item))
                    .forEach(newChildren::add);
            oldChildren.setAll(newChildren);
        });
    }

    @Override
    public String toString() {
        return getPathText(null, false, rootLabel);
    }

    private static String getPathText(Path path, boolean full, String rootLabel) {
        if (path != null) {
            if (full || path.getFileName() == null) {
                try {
                    if (Files.isSameFile(path, path.getRoot())) {
                        return FileSystemView.getFileSystemView().getSystemDisplayName(path.toFile());
                    }
                } catch (IOException e) {
                    return path.toString();
                }
                return path.toString();
            }
            return path.getFileName().toString();
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException x) {
            return rootLabel;
        }
    }

    private static final class FileTreeCell extends TreeCell<Path> {
        private final BiFunction<Path, Boolean, Optional<Node>> iconProvider;
        private final String rootLabel;

        private FileTreeCell(String rootLabel,
                             BiFunction<Path, Boolean, Optional<Node>> iconProvider) {
            this.rootLabel = rootLabel;
            this.iconProvider = iconProvider;
        }

        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTreeItem() == null) {
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

        private FileTreeItem(Path path, FileWatcher fileWatcher,
                             BiFunction<Path, Boolean, Optional<Node>> iconProvider,
                             BiConsumer<TreeItem<Path>, Boolean> navigate) {
            super(path);
            this.fileWatcher = fileWatcher;
            this.iconProvider = iconProvider;
            this.navigate = navigate;
            if (path != null) {
                setValue(path);
                if (Files.isDirectory(path)) {
                    addEventHandler(TreeItem.branchCollapsedEvent(), (TreeModificationEvent<Path> e) -> {
                        TreeItem<Path> treeItem = e.getSource();
                        iconProvider.apply(treeItem.getValue(), e.getSource().isExpanded())
                                .ifPresent(treeItem::setGraphic);
                        treeItem.getChildren().clear();
                        navigate.accept(treeItem, false);
                    });
                    addEventHandler(TreeItem.branchExpandedEvent(), (TreeModificationEvent<Path> e) -> {
                        TreeItem<Path> treeItem = e.getSource();
                        updateChildren(treeItem);
                        iconProvider.apply(treeItem.getValue(), treeItem.isExpanded())
                                .ifPresent(treeItem::setGraphic);
                        fetch = false;
                        navigate.accept(treeItem, true);
                    });
                    fileWatcher.register(getValue(), ev -> updateChildren(this));
                }
                iconProvider.apply(path, false).ifPresent(this::setGraphic);
                parentProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        fetch = false;
                        fileWatcher.unregister(path);
                    }
                });
            }
        }

        private Boolean updateChildren(TreeItem<Path> treeItem) {
            ObservableList<TreeItem<Path>> oldChildren = treeItem.getChildren();
            fetch = true;
            ObservableList<TreeItem<Path>> newChildren = findNewChildren(oldChildren, navigate);
            oldChildren.setAll(newChildren);
            return fetch;
        }

        @SuppressWarnings("TypeMayBeWeakened")
        private ObservableList<TreeItem<Path>> findNewChildren(ObservableList<TreeItem<Path>> oldChildren,
                                                               BiConsumer<TreeItem<Path>, Boolean> collapse) {
            ObservableList<TreeItem<Path>> newChildren = FXCollections.observableArrayList(oldChildren);
            newChildren.removeIf(item -> !Files.isReadable(item.getValue()));
            FolderChooser.findChildren(getValue(), fileWatcher, iconProvider, collapse)
                    .stream().filter(item -> Files.isReadable(item.getValue())
                    && !oldChildren.contains(item))
                    .forEach(newChildren::add);
            return newChildren;
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            return obj instanceof TreeItem && getValue().equals(((TreeItem<Path>) obj).getValue());
        }

        @Override
        public boolean isLeaf() {
            return Files.isRegularFile(getValue());
        }

        @Override
        public ObservableList<TreeItem<Path>> getChildren() {
            ObservableList<TreeItem<Path>> oldChildren = super.getChildren();
            if (fetch) {
                return oldChildren;
            }
            fetch = true;
            ObservableList<TreeItem<Path>> newChildren = findNewChildren(oldChildren, navigate);
            oldChildren.setAll(newChildren);
            return newChildren;
        }
    }

    private static final class FileWatcher implements Closeable {
        private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);
        private final Map<Path, FileWatcherData> watch = new ConcurrentHashMap<>(2);
        private volatile boolean running;

        @Override
        public void close() {
            running = false;
        }

        private void register(Path path, Callback<FileWatcherEvent, Boolean> callback) {
            if (path != null && Files.isReadable(path) && !watch.containsKey(path)) {
                try {
                    WatchService watchService = path.getFileSystem().newWatchService();
                    path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    watch.put(path, new FileWatcherData(path, callback, watchService));
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }

        private void start() {
            Thread thread = createThread();
            thread.start();
        }

        @SuppressWarnings({"BusyWait", "NestedAssignment", "MethodCallInLoopCondition"})
        private Thread createThread() {
            return new Thread(() -> {
                running = true;
                while (running) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.error(e.getLocalizedMessage(), e);
                    }
                    Iterator<Map.Entry<Path, FileWatcherData>> iterator = watch.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Path, FileWatcherData> entry = iterator.next();
                        Path path = entry.getKey();
                        FileWatcherData data = entry.getValue();
                        if (data != null) {
                            WatchService watchService = data.getWatchService();
                            Path dataPath = data.getPath();
                            try {
                                WatchKey key;
                                List<FileWatcherEventData> watchPaths = new LinkedList<>();
                                while ((key = watchService.poll()) != null) {
                                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                                        if (path == null || dataPath.equals(path)) {
                                            watchPaths.add(new FileWatcherEventData(
                                                    dataPath.resolve((Path) watchEvent.context()),
                                                    watchEvent.kind(),
                                                    watchEvent.count()));
                                        }
                                    }
                                    key.reset();
                                }
                                if (!watchPaths.isEmpty() && path != null) {
                                    data.getCallback().call(new FileWatcherEvent(path, watchPaths));
                                }
                            } catch (ClosedWatchServiceException e) {
                                iterator.remove();
                            }
                        }
                    }
                }
                for (Map.Entry<Path, FileWatcherData> entry : watch.entrySet()) {
                    FileWatcherData data = watch.get(entry.getKey());
                    if (data != null) {
                        WatchService watchService = data.getWatchService();
                        try {
                            watchService.close();
                        } catch (IOException e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }, FileWatcher.class.getName());
        }

        private void unregister(Path path) {
            FileWatcherData data = watch.get(path);
            if (data != null) {
                try {
                    data.getWatchService().close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                watch.remove(path);
            }
        }
    }

    private static final class FileWatcherData {
        private final Callback<FileWatcherEvent, Boolean> callback;
        private final Path path;
        private final WatchService watchService;

        private FileWatcherData(Path path, Callback<FileWatcherEvent, Boolean> callback,
                                WatchService watchService) {
            this.path = path;
            this.callback = callback;
            this.watchService = watchService;
        }

        private Callback<FileWatcherEvent, Boolean> getCallback() {
            return callback;
        }

        private Path getPath() {
            return path;
        }

        private WatchService getWatchService() {
            return watchService;
        }
    }

    private static final class FileWatcherEvent {
        private final Path registeredPath;
        private final List<FileWatcherEventData> watcherEventData;

        private FileWatcherEvent(Path registeredPath, List<FileWatcherEventData> watcherEventData) {
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

        private FileWatcherEventData(Path path, WatchEvent.Kind<?> kind, int count) {
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
