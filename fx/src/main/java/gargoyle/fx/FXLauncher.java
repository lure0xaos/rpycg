package gargoyle.fx;

import gargoyle.fx.log.FXLog;
import javafx.application.Application;
import javafx.event.Event;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.awt.Window;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.LogManager;

public final class FXLauncher {
    static final String KEY_APP = FXWrapper.class.getName();
    private static FXSplash splash;
    private static Window splashWindow;

    static {
        try {
            LogManager.getLogManager().readConfiguration(FXLauncher.class.getResourceAsStream("logging.properties"));
        } catch (final IOException e) {
            FXLog.error(e, FXUtil.format("{location} initialization error", Map.of("location", "log")));
        }
    }

    private FXLauncher() {
        throw new IllegalStateException(FXLauncher.class.getName());
    }

    public static void exit(final FXContext context) {
        final Stage primaryStage = context.getStage();
        Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public static void notifySplash(final FXSplash.FXSplashNotification.Type type,
                                    final double progress, final String details) {
        notifySplash(new FXSplashNotificationImpl(type, progress, details));
    }

    public static void notifySplash(final FXSplash.FXSplashNotification splashNotification) {
        if (null != splash) {
            splash.handleSplashNotification(splashNotification);
        }
    }

    public static void requestPrevent(final FXContext context, final Callback<FXContext, FXCloseAction> callback) {
        FXCloseFlag.PREVENT.set(context, callback);
    }

    public static void requestRestart(final FXContext context) {
        FXCloseFlag.RESTART.set(context, fxContext -> {
            final FXCloseAction saved = FXCloseFlag.PREVENT.doIf(fxContext);
            try {
                final Stage stage = fxContext.getStage();
                final FXWrapper wrapper = getWrapper(fxContext);
                wrapper.askStop();
                wrapper.init();
                wrapper.start(stage);
            } catch (final Exception e) {
                FXLog.error(e, FXUtil.format("{location} initialization error",
                        Map.of("location", "close")));
                return FXCloseAction.CLOSE;
            }
            return FXCloseAction.KEEP == saved ? FXCloseAction.CLOSE : FXCloseAction.KEEP;
        });
        exit(context);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void run(final Class<? extends FXApplication> appClass, final String[] args) {
        try {
            FXContextFactory.builder().setBaseClass(appClass).setLocale(Locale.getDefault());
            splashStart(appClass);
            final String[] newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = appClass.getName();
            Application.launch(FXWrapper.class, newArgs);
        } catch (final Exception e) {
            FXLog.error(e, FXConstants.KEY_SPLASH);
            splashStop("");
        }
    }

    static void splashStop(final String details) {
        if (null != splashWindow) {
            if (splashWindow.isVisible()) {
                notifySplash(FXSplash.FXSplashNotification.Type.STOP, 1, details);
                splashWindow.dispose();
            }
        }
    }

    private static FXWrapper getWrapper(final FXContext fxContext) {
        return (FXWrapper) fxContext.getStage().getProperties().get(KEY_APP);
    }

    private static void splashStart(final Class<? extends FXApplication> appClass) {
        final String splashClassName = AccessController.doPrivileged((PrivilegedAction<String>) () ->
                System.getProperty(FXConstants.KEY_SPLASH_CLASS, FXConstants.SPLASH_CLASS_DEFAULT));
        try {
            final Class<? extends FXSplash> splashClass = FXIntUtil.loadClass(splashClassName, appClass.getClassLoader());
            splash = FXIntUtil.newInstance(splashClass);
            final String splashLocation = AccessController.doPrivileged((PrivilegedAction<String>) () ->
                    System.getProperty(FXConstants.KEY_SPLASH));
            splashWindow = splash.createWindow(null != splashLocation ? appClass.getResource(splashLocation)
                            : FXWrapper.class.getResource(FXConstants.SPLASH_DEFAULT),
                    ResourceBundle.getBundle(FXUtil.resolveBaseName(splashClass)));
            splashWindow.pack();
            splashWindow.setAlwaysOnTop(true);
            splashWindow.setLocationRelativeTo(null);
            splashWindow.setVisible(true);
            notifySplash(FXSplash.FXSplashNotification.Type.PRE_INIT, 0.0, "");
        } catch (final MissingResourceException | FXException e) {
            FXLog.warn(FXUtil.format("No resources {location}", Map.of("location", splashClassName)));
        }
    }

    public static final class FXSplashNotificationImpl implements FXSplash.FXSplashNotification {
        private final String details;
        private final double progress;
        private final Type type;

        private FXSplashNotificationImpl(final Type type, final double progress, final String details) {
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
