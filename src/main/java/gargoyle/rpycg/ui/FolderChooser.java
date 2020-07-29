package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLoad;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FolderChooser extends Dialog<File> implements Initializable {

    private static final String ICON_COMPUTER = "icons/computer";
    private static final String ICON_FILE = "icons/text-x-generic";
    private static final String ICON_FOLDER = "icons/folder";
    private static final String ICON_FOLDER_OPEN = "icons/folder-open";
    private final FileWatcher fileWatcher;
    private final Property<File> initialDirectory;
    @FXML
    private TreeView<Path> fileTree;
    private String rootLabel;

    public FolderChooser() {
        fileWatcher = new FileWatcher();
        initialDirectory = new SimpleObjectProperty<>(null);
        FXLoad.loadDialog(FXContextFactory.currentContext(), this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }

    public File getInitialDirectory() {
        return initialDirectory.getValue();
    }

    public void setInitialDirectory(File initialDirectory) {
        this.initialDirectory.setValue(initialDirectory);
    }

    @SuppressWarnings({"MethodCallInLoopCondition", "ReturnOfNull"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXDialogs.decorateDialog(FXContextFactory.currentContext(), this, resources, param ->
                param.getButtonData().isCancelButton() ? null :
                fileTree.getSelectionModel().getSelectedItem().getValue().toFile());
        setResizable(true);
        rootLabel = resources.getString("root");
        fileTree.setCellFactory(param -> new FileTreeCell(FXContextFactory.currentContext(), rootLabel));
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TreeItem<Path> rootNode = new TreeItem<>(null,
                getPathGraphic(FXContextFactory.currentContext(), null, false).orElse(null));
        rootNode.getChildren().setAll(findChildren(FXContextFactory.currentContext(), null, fileWatcher));
        rootNode.setExpanded(true);
        fileTree.setRoot(rootNode);
        initialDirectory.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TreeItem<Path> item = fileTree.getRoot();
                Path curPath = newValue.toPath();
                while (!Files.exists(curPath) || !Files.isReadable(curPath)) {
                    curPath = curPath.getParent();
                }
                List<Path> paths = new ArrayList<>(newValue.toPath().getNameCount());
                while (curPath != null) {
                    paths.add(0, curPath);
                    curPath = curPath.getParent();
                }
                Collection<TreeItem<Path>> collect = new LinkedList<>();
                for (Path path : paths) {
                    for (TreeItem<Path> child : item.getChildren()) {
                        if (path.equals(child.getValue())) {
                            item = child;
                            collect.add(item);
                        }
                    }
                }
                collect.forEach(treeItem -> {
                    treeItem.setExpanded(true);
                    fileTree.getSelectionModel().select(treeItem);
                });
                fileTree.scrollTo(fileTree.getRow(item));
            }
        });
        Platform.runLater(() -> fileTree.requestFocus());
    }

    private static Optional<Node> getPathGraphic(FXContext context, Path path, boolean expanded) {
        @NotNull String name = path == null ? ICON_COMPUTER :
                               Files.isDirectory(path) ? expanded ? ICON_FOLDER_OPEN : ICON_FOLDER : ICON_FILE;
        return FXLoad.findResource(context, FXLoad.getBaseName(FolderChooser.class, name), FXLoad.IMAGES).map(URL::toExternalForm).map(ImageView::new);
    }

    private static ObservableList<TreeItem<Path>> findChildren(FXContext context,
                                                               Path path, FileWatcher fileWatcher) {
        if (path == null) {
            return FXCollections.observableArrayList(
                    StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)
                            .filter(Files::isDirectory).sorted()
                            .map(watchPath -> new FileTreeItem(context, watchPath, fileWatcher))
                            .collect(Collectors.toList()));
        }
        if (Files.isDirectory(path)) {
            try (Stream<Path> list = Files.list(path)) {
                return FXCollections.observableArrayList(list.filter(Files::isDirectory).sorted()
                        .map(watchPath -> new FileTreeItem(context, watchPath, fileWatcher))
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                return FXCollections.observableArrayList();
            }
        }
        return FXCollections.observableArrayList();
    }

    public File showDialog(Stage stage) {
        fileWatcher.start();
        if (!stage.isShowing()) {
            initOwner(stage);
        }
        Optional<File> file = showAndWait();
        fileWatcher.close();
        file.ifPresent(initialDirectory::setValue);
        return file.orElse(null);
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

        private final FXContext context;
        private final String rootLabel;

        private FileTreeCell(FXContext context, String rootLabel) {
            this.rootLabel = rootLabel;
            this.context = context;
        }

        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTreeItem() == null) {
                setGraphic(null);
            } else {
                getPathGraphic(context, item, getTreeItem().isExpanded()).ifPresent(this::setGraphic);
            }
            setText(empty ? null : getPathText(item, false, rootLabel));
            setTooltip(empty ? null : new Tooltip(getPathText(item, true, rootLabel)));
        }
    }

    private static final class FileTreeItem extends TreeItem<Path> {
        private final FileWatcher fileWatcher;
        private final FXContext context;
        private boolean fetch;

        private FileTreeItem(FXContext context, Path path, FileWatcher fileWatcher) {
            super(path);
            this.fileWatcher = fileWatcher;
            this.context = context;
            if (path != null) {
                setValue(path);
                if (Files.isDirectory(path)) {
                    addEventHandler(TreeItem.branchCollapsedEvent(), (EventHandler<TreeModificationEvent<Path>>) e -> {
                        TreeItem<Path> treeItem = e.getSource();
                        getPathGraphic(this.context, treeItem.getValue(), e.getSource().isExpanded())
                                .ifPresent(treeItem::setGraphic);
                    });
                    addEventHandler(TreeItem.branchExpandedEvent(), (EventHandler<TreeModificationEvent<Path>>) e -> {
                        TreeItem<Path> treeItem = e.getSource();
                        getPathGraphic(this.context, treeItem.getValue(), treeItem.isExpanded())
                                .ifPresent(treeItem::setGraphic);
                        fetch = false;
                    });
                    fileWatcher.register(getValue(), ev -> updateChildren(this));
                }
            }
            getPathGraphic(this.context, path, false).ifPresent(this::setGraphic);
            parentProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    fetch = false;
                    fileWatcher.unregister(path);
                }
            });
        }

        private Boolean updateChildren(TreeItem<Path> treeItem) {
            ObservableList<TreeItem<Path>> oldChildren = treeItem.getChildren();
            fetch = true;
            ObservableList<TreeItem<Path>> newChildren = findNewChildren(oldChildren);
            oldChildren.setAll(newChildren);
            return true;
        }

        @SuppressWarnings("TypeMayBeWeakened")
        @NotNull
        private ObservableList<TreeItem<Path>> findNewChildren(ObservableList<TreeItem<Path>> oldChildren) {
            ObservableList<TreeItem<Path>> newChildren = FXCollections.observableArrayList(oldChildren);
            newChildren.removeIf(item -> !Files.exists(item.getValue()) || !Files.isReadable(item.getValue()));
            findChildren(context, getValue(), fileWatcher)
                    .stream().filter(item -> Files.exists(item.getValue()) && Files.isReadable(item.getValue())
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
            ObservableList<TreeItem<Path>> newChildren = findNewChildren(oldChildren);
            oldChildren.setAll(newChildren);
            return newChildren;
        }
    }

    private static final class FileWatcher implements Closeable {
        private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);
        private final Map<Path, FileWatcherData> watch = new ConcurrentHashMap<>();
        private volatile boolean running;

        @Override
        public void close() {
            running = false;
        }

        public void register(Path path, Callback<FileWatcherEvent, Boolean> callback) {
            if (path != null && Files.exists(path) && Files.isReadable(path)
                && !watch.containsKey(path)) {
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

        public void start() {
            Thread thread = createThread();
            thread.start();
        }

        @NotNull
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
                            if (watchService != null && dataPath != null) {
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
                                    if (!watchPaths.isEmpty()) {
                                        data.getCallback().call(new FileWatcherEvent(path, watchPaths));
                                    }
                                } catch (ClosedWatchServiceException e) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
                for (Map.Entry<Path, FileWatcherData> entry : watch.entrySet()) {
                    FileWatcherData data = watch.get(entry.getKey());
                    if (data != null) {
                        WatchService watchService = data.getWatchService();
                        if (watchService != null) {
                            try {
                                watchService.close();
                            } catch (IOException e) {
                                log.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            }, FileWatcher.class.getName());
        }

        public void unregister(Path path) {
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

        private FileWatcherData(Path path, Callback<FileWatcherEvent, Boolean> callback, WatchService watchService) {
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
