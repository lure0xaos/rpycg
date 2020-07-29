package gargoyle.rpycg.ui;

import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public final class RPyCGApp extends Application {

    public static void run(String[] args) {
        launch(RPyCGApp.class, args);
    }

    @Override
    public void init() {
        FXContextFactory.initializeContext(this);
    }

    @Override
    public void start(Stage primaryStage) {
        FXContext context = FXContextFactory.currentContext();
        FXLoad.loadResources(context, FXLoad.getBaseName(getClass()))
                .ifPresent(resources -> primaryStage.setTitle(resources.getString("title")));
        FXLoad.findResource(context, FXLoad.getBaseName(getClass()), FXLoad.IMAGES)
                .map(URL::toExternalForm).map(Image::new)
                .ifPresent(url -> primaryStage.getIcons().add(url));
        primaryStage.setScene(new Scene(new Main()));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
