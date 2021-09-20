package gargoyle.rpycg.fx;

import gargoyle.rpycg.fx.log.FXLog;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.Component;
import java.awt.Window;
import java.util.Arrays;

public final class FXWrapper extends Application {
    private static final String LC_TITLE = "title";
    private Class<? extends FXApplication> appClass;
    private String appClassName;
    private FXContext context;

    public FXWrapper() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        try {
            appClassName = getParameters().getRaw().get(0);
            appClass = FXIntUtil.loadClass(appClassName, getClass().getClassLoader());
            context = FXContextFactory.currentContext();
            context.register(appClassName, (Class<FXApplication>) appClass, (Class<FXApplication>) appClass);
            context.initializeContext(getHostServices(), getParameters());
            FXLauncher.notifySplash(FXSplash.FXSplashNotification.Type.INIT, 0.0, "");
            final FXApplication application = context.getBean(appClassName, appClass);
            application.doInit();
        } catch (final Exception e) {
            FXLauncher.splashStop("");
            FXLog.error(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
            throw new FXUserException(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        }
    }

    @Override
    public void start(final Stage primaryStage) {
        try {
            context.register(Stage.class, primaryStage);
            primaryStage.getProperties().put(FXLauncher.KEY_APP, this);
            primaryStage.setOnCloseRequest(event -> {
                try {
                    for (final FXCloseFlag closeFlag : FXCloseFlag.values()) {
                        if (FXCloseAction.KEEP == closeFlag.doIf(context)) {
                            event.consume();
                            break;
                        }
                    }
                } catch (final Exception e) {
                    FXLog.error(e, e.getLocalizedMessage());
                }
            });
            FXLauncher.notifySplash(FXSplash.FXSplashNotification.Type.START, 0.0, "");
            FXIntUtil.prepareStage(context, appClassName, primaryStage);
            FXIntUtil.loadResources(context.getBaseClass(), context.getLocale(),
                    FXUtil.resolveBaseName(appClass)).ifPresent(resources ->
                    primaryStage.setTitle(resources.getString(LC_TITLE)));
            primaryStage.setScene(FXUtil.getOrCreateScene(context.getBean(appClassName, appClass).doStart().getView()));
            primaryStage.centerOnScreen();
            primaryStage.show();
            FXLauncher.splashStop("");
        } catch (final Exception e) {
            FXLauncher.splashStop(FXUtil.message(FXUserException.class, context.getLocale(),
                    FXUserException.LC_ERROR_INITIALIZATION, appClassName));
            FXLog.error(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
            throw new FXUserException(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        }
    }

    @Override
    public void stop() {
        try {
            askStop();
            Arrays.stream(Window.getWindows()).filter(Component::isDisplayable).forEach(Window::dispose);
        } catch (final Exception e) {
            FXLog.error(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
            throw new FXUserException(e, FXUserException.LC_ERROR_INITIALIZATION, appClassName);
        }
    }

    void askStop() throws Exception {
        try {
            context.getBean(appClassName, appClass).doStop();
        } finally {
            FXLauncher.splashStop(FXUtil.message(FXUserException.class, context.getLocale(),
                    FXUserException.LC_ERROR_INITIALIZATION, appClassName));
            context.getRegistry().close();
        }
    }

}
