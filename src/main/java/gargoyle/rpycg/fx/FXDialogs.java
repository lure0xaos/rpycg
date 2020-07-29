package gargoyle.rpycg.fx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class FXDialogs {
    private static final String ICON_CANCEL = "/gargoyle/rpycg/ui/icons/cancel";
    private static final String ICON_EMPTY = "/gargoyle/rpycg/ui/icons/empty";
    private static final String ICON_OK = "/gargoyle/rpycg/ui/icons/ok";
    private static final String KEY_CANCEL = "cancel";
    private static final String KEY_OK = "ok";
    private static final String KEY_TITLE = "title";
    private static final Logger log = LoggerFactory.getLogger(FXDialogs.class);

    private FXDialogs() {
        throw new IllegalStateException(String.valueOf(getClass()));
    }

    public static void alert(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        Alert dialog = new Alert(AlertType.INFORMATION);
        decorateDialogAs(context, owner, dialog);
        dialog.setTitle(getTitle(context, owner, "information", "Information"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        dialog.showAndWait();
    }

    public static <R> void decorateDialog(@NotNull FXContext context, Dialog<R> dialog, ResourceBundle resources,
                                          Callback<ButtonType, R> resultConverter) {
        dialog.setTitle(resources.getString(KEY_TITLE));
        dialog.setDialogPane(new DecoratedDialogPane((buttonData, button) ->
                FXLoad.findResource(context, FXLoad.getBaseName(dialog.getClass(), getIconPath(buttonData)),
                        FXLoad.IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new)
                        .ifPresent(button::setGraphic)));
        dialog.getDialogPane().getButtonTypes().setAll(
                new ButtonType(resources.getString(KEY_OK), ButtonBar.ButtonData.OK_DONE),
                new ButtonType(resources.getString(KEY_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        dialog.setResultConverter(resultConverter);
    }

    @NotNull
    private static String getTitle(@NotNull FXContext context,
                                   @Nullable Stage owner, @NotNull String key, @NotNull String defaultTitle) {
        return getTitle(owner, () -> getString(context, key, defaultTitle));
    }

    public static void decorateDialogAs(@NotNull FXContext context,
                                        @Nullable Stage primaryStage, @Nullable Dialog<?> dialog) {
        decorateDialogAs(primaryStage, dialog, (buttonData, button) -> FXLoad.findResource(context,
                FXLoad.getBaseName(FXDialogs.class, getIconPath(buttonData)), FXLoad.IMAGES)
                .map(URL::toExternalForm).map(ImageView::new)
                .ifPresent(button::setGraphic));
    }

    public static void decorateDialogAs(@Nullable Stage primaryStage, @Nullable Dialog<?> dialog,
                                        @NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator) {
        decorateDialogButtons(dialog, buttonDecorator);
        Optional.ofNullable(dialog).map(Dialog::getDialogPane).map(Node::getScene).map(Scene::getWindow)
                .filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(stage -> Objects.nonNull(primaryStage))
                .ifPresent(stage -> doDecorateStageAs(primaryStage, stage));
    }

    public static String getIconPath(ButtonBar.ButtonData buttonData) {
        switch (buttonData) {
            case OK_DONE:
            case YES:
                return ICON_OK;
            case CANCEL_CLOSE:
            case NO:
                return ICON_CANCEL;
            default:
                return ICON_EMPTY;
        }
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        return confirm(context, owner, message, ButtonType.OK, ButtonType.CANCEL)
                .map(buttonType -> buttonType == ButtonType.OK).orElse(Boolean.FALSE);
    }

    @NotNull
    public static String getTitle(@Nullable Stage owner, @NotNull Supplier<String> titleProvider) {
        return Optional.ofNullable(owner).map(Stage::getTitle).orElseGet(titleProvider);
    }

    private static void decorateDialogButtons(@Nullable Dialog<?> dialog,
                                              @NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator) {
        Optional.ofNullable(dialog).ifPresent(window -> {
            DialogPane oldDialogPane = window.getDialogPane();
            window.setDialogPane(new DecoratedDialogPane(buttonDecorator));
            window.getDialogPane().getButtonTypes().setAll(oldDialogPane.getButtonTypes());
        });
    }

    private static void doDecorateStageAs(@NotNull Stage primaryStage, @NotNull Stage window) {
        window.setTitle(primaryStage.getTitle());
        window.getIcons().setAll(primaryStage.getIcons());
    }

    public static Optional<ButtonType> confirm(@NotNull FXContext context,
                                               @Nullable Stage owner, @NotNull String message, ButtonType... buttons) {
        Alert dialog = new Alert(AlertType.CONFIRMATION);
        decorateDialogAs(context, owner, dialog);
        dialog.setTitle(getTitle(context, owner, "confirmation", "Confirmation"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.getButtonTypes().setAll(buttons);
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    @NotNull
    private static String getString(@NotNull FXContext context, @NotNull String key, @NotNull String defaultTitle) {
        return FXLoad.loadResources(context, FXLoad.getBaseName(FXDialogs.class))
                .map(resources -> resources.getString(key))
                .orElse(defaultTitle);
    }

    private static void setModal(@Nullable Stage owner, @NotNull Dialog<?> dialog) {
        Optional.ofNullable(owner).ifPresent(stage -> {
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
        });
    }

    public static void error(@NotNull FXContext context, @Nullable Stage owner,
                             @NotNull String message, @Nullable Exception ex) {
        if (ex != null) {
            log.info(ex.getLocalizedMessage(), ex);
        }
        Alert dialog = new Alert(AlertType.ERROR);
        decorateDialogAs(context, owner, dialog);
        dialog.setTitle(getTitle(context, owner, "error", "Error"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        if (ex != null) {
            Label label = new Label(ex.getClass().getName());
            String exceptionText;
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
                exceptionText = sw.toString();
            } catch (IOException e) {
                log.error("unexpected", e);
                exceptionText = ex.getLocalizedMessage();
            }
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
            dialog.getDialogPane().setExpandableContent(expContent);
        }
        setModal(owner, dialog);
        dialog.showAndWait();
    }

    public static void error(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        Alert dialog = new Alert(AlertType.INFORMATION);
        decorateDialogAs(context, owner, dialog);
        dialog.setTitle(getTitle(context, owner, "error", "Error"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        dialog.showAndWait();
    }

    @Nullable
    public static String prompt(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        return prompt(context, owner, message, "");
    }

    @Nullable
    public static String prompt(@NotNull FXContext context,
                                @Nullable Stage owner, @NotNull String message, @NotNull String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        decorateDialogAs(context, owner, dialog);
        dialog.setTitle(getTitle(context, owner, "prompt", "Please enter"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        return dialog.showAndWait().orElse(null);
    }

    private static final class DecoratedDialogPane extends DialogPane {
        @NotNull
        private final BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator;

        private DecoratedDialogPane(@NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator) {
            this.buttonDecorator = buttonDecorator;
        }

        @Override
        protected Node createButton(ButtonType buttonType) {
            Node node = super.createButton(buttonType);
            if (node instanceof Button) {
                Button button = (Button) node;
                ButtonBar.ButtonData buttonData = buttonType.getButtonData();
                buttonDecorator.accept(buttonData, button);
            }
            return node;
        }
    }
}
