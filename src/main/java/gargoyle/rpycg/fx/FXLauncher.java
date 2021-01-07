package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class FXLauncher extends Application {
    public static final String KEY_SPLASH = "fx.splash";
    public static final String KEY_SPLASH_CLASS = "fx.splash-class";
    private static final String KEY_APP = FXLauncher.class.getName();
    private static final String LC_TITLE = "title";
    private static final String SPLASH_CLASS_DEFAULT = FXImageSplash.class.getName();
    private static final String SPLASH_DEFAULT = "loading.gif";
    private static final Logger log = LoggerFactory.getLogger(FXLauncher.class);
    private static FXSplash splash;
    private static Window splashWindow;
    private Class<? extends FXApplication> appClass;
    private String appClassName;
    private FXApplication application;

    public static void requestPrevent(Stage primaryStage, Callback<Stage, FXCloseAction> callback) {
        FXCloseFlag.PREVENT.set(primaryStage, callback);
    }

    public static void requestRestart(Stage primaryStage) {
        FXCloseFlag.RESTART.set(primaryStage, stage -> {
            FXCloseAction saved = FXCloseFlag.PREVENT.doIf(stage);
            try {
                ((FXLauncher) stage.getProperties().get(KEY_APP)).restart(stage);
            } catch (Exception e) {
                log.error("", new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, "close"));
                return FXCloseAction.CLOSE;
            }
            return saved == FXCloseAction.KEEP ? FXCloseAction.CLOSE : FXCloseAction.KEEP;
        });
        FXLauncher.exit(primaryStage);
    }

    private void restart(Stage primaryStage) throws Exception {
        FXContext context = FXContextFactory.currentContext();
        FXContextFactory.initializeStage(primaryStage);
        String baseName = context.getBaseName(appClass);
        context.loadResources(baseName).ifPresent(resources -> primaryStage.setTitle(resources.getString(LC_TITLE)));
        context.findResource(baseName, FXConstants.EXT_IMAGES)
                .map(URL::toExternalForm).map(Image::new)
                .ifPresent(url -> primaryStage.getIcons().add(url));
        primaryStage.setScene(new Scene(application.doStart().getView()));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void exit(Stage primaryStage) {
        Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void run(Class<? extends FXApplication> appClass, String[] args) {
        try {
            splashStart(appClass);
        } catch (Exception e) {
            log.error(KEY_SPLASH, e);
        }
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = appClass.getName();
        Application.launch(FXLauncher.class, newArgs);
    }

    public static void splashStart(Class<? extends FXApplication> appClass) {
        String splashClassName = System.getProperty(KEY_SPLASH_CLASS, SPLASH_CLASS_DEFAULT);
        try {
            Class<? extends FXSplash> splashClass = FXReflection.classForName(splashClassName);
            splash = FXReflection.instantiate(splashClass);
            String splashLocation = System.getProperty(KEY_SPLASH);
            splashWindow = splash.createWindow(splashLocation != null ?
                            appClass.getResource(splashLocation) : FXLauncher.class.getResource(SPLASH_DEFAULT),
                    ResourceBundle.getBundle(FXContextFactory.currentContext().getBaseName(splashClass)));
            splashWindow.pack();
            splashWindow.setAlwaysOnTop(true);
            splashWindow.setLocationRelativeTo(null);
            splashWindow.setVisible(true);
            notifySplash(FXSplash.FXSplashNotification.Type.PRE_INIT, 0, "");
        } catch (MissingResourceException | FXException e) {
            log.warn(new FXUserException(FXUserException.LC_ERROR_NO_RESOURCES, splashClassName).getMessage());
        }
    }

    public static void notifySplash(FXSplash.FXSplashNotification.Type type, double progress, String details) {
        notifySplash(new FXSplashNotificationImpl(type, progress, details));
    }

    public static void notifySplash(FXSplash.FXSplashNotification splashNotification) {
        if (splash != null) {
            splash.handleSplashNotification(splashNotification);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        try {
            appClassName = getParameters().getRaw().get(0);
            appClass = (Class<? extends FXApplication>) Class.forName(appClassName);
            application = FXContextFactory.currentContext().getBean(appClass);
            FXContextFactory.initializeContext(this, appClass);
            notifySplash(FXSplash.FXSplashNotification.Type.INIT, 0, "");
            application.doInit();
        } catch (Exception e) {
            splashStop(new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, appClassName).getMessage());
            throw new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getProperties().put(KEY_APP, this);
        primaryStage.setOnCloseRequest(event -> {
            for (FXCloseFlag closeFlag : FXCloseFlag.values()) {
                if (closeFlag.doIf(primaryStage) == FXCloseAction.KEEP) {
                    event.consume();
                    break;
                }
            }
        });
        try {
            notifySplash(FXSplash.FXSplashNotification.Type.START, 0, "");
            restart(primaryStage);
            splashStop("");
        } catch (Exception e) {
            splashStop(new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, appClassName).getMessage());
            throw new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        }
    }

    @Override
    public void stop() {
        try {
            application.doStop();
        } catch (Exception e) {
            splashStop("");
            throw new FXUserException(FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        } finally {
            Arrays.stream(Window.getWindows()).filter(Component::isDisplayable).forEach(Window::dispose);
        }
    }

    private void splashStop(String details) {
        if (splash != null) {
            if (splashWindow.isVisible()) {
                notifySplash(FXSplash.FXSplashNotification.Type.STOP, 1, details);
                splashWindow.dispose();
            }
        }
    }

    public enum FXCloseAction {
        CLOSE, KEEP
    }

    private enum FXCloseFlag {
        RESTART,
        PREVENT;

        @SuppressWarnings("unchecked")
        private FXCloseAction doIf(Stage primaryStage) {
            ObservableMap<Object, Object> primaryStageProperties = primaryStage.getProperties();
            if (primaryStageProperties.containsKey(this)) {
                Callback<Stage, FXCloseAction> callback = (Callback<Stage, FXCloseAction>)
                        primaryStageProperties.get(this);
                primaryStageProperties.remove(this);
                return callback.call(primaryStage);
            }
            return FXCloseAction.CLOSE;
        }

        private void set(Stage primaryStage, Callback<Stage, FXCloseAction> callback) {
            primaryStage.getProperties().put(this, callback);
        }
    }

    public static final class FXSplashNotificationImpl implements FXSplash.FXSplashNotification {
        private final String details;
        private final double progress;
        private final Type type;

        private FXSplashNotificationImpl(Type type, double progress, String details) {
            this.type = type;
            this.progress = progress;
            this.details = details;
        }

        @Override
        public String getDetails() {
            return details;
        }

        @Override
        public double getProgress() {
            return progress;
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
