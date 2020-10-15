package gargoyle.rpycg.ui;

import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import javafx.application.Application;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.net.URL;

public final class RPyCGApp extends Application {

    private static final String KEY_APP = "RPyCGApp";
    private static final String LC_TITLE = "title";

    public static void requestPrevent(Stage primaryStage, Callback<Stage, CloseAction> callback) {
        CloseFlag.PREVENT.set(primaryStage, callback);
    }

    public static void requestRestart(Stage primaryStage) {
        CloseFlag.RESTART.set(primaryStage, stage -> {
            CloseAction saved = CloseFlag.PREVENT.doIf(stage);
            ((RPyCGApp) stage.getProperties().get(KEY_APP)).doStart(stage);
            return saved == CloseAction.KEEP ? CloseAction.CLOSE : CloseAction.KEEP;
        });
        exit(primaryStage);
    }

    private void doStart(Stage primaryStage) {
        FXContext context = FXContextFactory.currentContext();
        FXLoad.loadResources(context, FXLoad.getBaseName(getClass()))
                .ifPresent(resources -> primaryStage.setTitle(resources.getString(LC_TITLE)));
        FXLoad.findResource(context, FXLoad.getBaseName(getClass()), FXLoad.IMAGES)
                .map(URL::toExternalForm).map(Image::new)
                .ifPresent(url -> primaryStage.getIcons().add(url));
        primaryStage.setScene(new Scene(new Main()));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void exit(Stage primaryStage) {
        Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void run(String[] args) {
        launch(RPyCGApp.class, args);
    }

    @Override
    public void init() {
        FXContextFactory.initializeContext(this);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getProperties().put(KEY_APP, this);
        primaryStage.setOnCloseRequest(event -> {
            for (CloseFlag closeFlag : CloseFlag.values()) {
                if (closeFlag.doIf(primaryStage) == CloseAction.KEEP) {
                    event.consume();
                    break;
                }
            }
        });
        doStart(primaryStage);
    }

    public enum CloseAction {
        CLOSE, KEEP
    }

    private enum CloseFlag {
        RESTART,
        PREVENT;

        @SuppressWarnings("unchecked")
        private CloseAction doIf(Stage primaryStage) {
            ObservableMap<Object, Object> primaryStageProperties = primaryStage.getProperties();
            if (primaryStageProperties.containsKey(this)) {
                Callback<Stage, CloseAction> callback = (Callback<Stage, CloseAction>) primaryStageProperties.get(this);
                primaryStageProperties.remove(this);
                return callback.call(primaryStage);
            }
            return CloseAction.CLOSE;
        }

        private void set(Stage primaryStage, Callback<Stage, CloseAction> callback) {
            primaryStage.getProperties().put(this, callback);
        }
    }
}
