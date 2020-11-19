package gargoyle.rpycg.fx;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class FXDialogs {
    private static final String ICON_CANCEL = "/gargoyle/rpycg/ui/icons/cancel";
    private static final String ICON_EMPTY = "/gargoyle/rpycg/ui/icons/empty";
    private static final String ICON_OK = "/gargoyle/rpycg/ui/icons/ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_APPLY = "apply";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_CANCEL = "cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_CONFIRMATION = "confirmation";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_INFORMATION = "information";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_LESS = "less";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_MORE = "more";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_NO = "no";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_OK = "ok";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs")
    private static final String LC_YES = "yes";
    private static final Logger log = LoggerFactory.getLogger(FXDialogs.class);

    private FXDialogs() {
        throw new IllegalStateException(FXDialogs.class.getName());
    }

    @NotNull
    public static Optional<ButtonType> alert(@Nullable Stage owner, @NotNull String message,
                                             @NotNull ButtonType... buttons) {
        return alert(FXContextFactory.currentContext(), owner, message, buttons);
    }

    @NotNull
    public static Optional<ButtonType> alert(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message,
                                             @NotNull ButtonType... buttons) {
        return dialog(context, owner, AlertType.INFORMATION, message,
                () -> getString(context, LC_INFORMATION, LC_INFORMATION), asMap(buttons));
    }

    @NotNull
    public static Optional<ButtonType> dialog(@NotNull FXContext context,
                                              @Nullable Stage owner,
                                              @NotNull AlertType type,
                                              @NotNull String message,
                                              @NotNull Supplier<String> titleProvider,
                                              @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        Alert dialog = buttons == null || buttons.isEmpty() ?
                new Alert(type, message) :
                new Alert(type, message, asButtonTypeArray(buttons));
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(owner, titleProvider));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    @NotNull
    private static String getString(@NotNull FXContext context,
                                    @NotNull @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXDialogs") String key,
                                    @NotNull String defaultTitle) {
        return context.loadResources(FXDialogs.class)
                .map(resources -> {
                    try {
                        return resources.getString(key);
                    } catch (MissingResourceException e) {
                        log.warn(new FXUserException(FXUserException.LC_ERROR_NO_RESOURCES, key).getMessage());
                        return key;
                    }
                })
                .orElse(defaultTitle);
    }

    @NotNull
    private static Map<ButtonBar.ButtonData, String> asMap(ButtonType[] buttons) {
        return Arrays.stream(buttons).collect(Collectors.toMap(ButtonType::getButtonData, ButtonType::getText));
    }

    @NotNull
    public static ButtonType[] asButtonTypeArray(@NotNull Map<ButtonBar.ButtonData, String> buttons) {
        return buttons.entrySet().stream()
                .map(entry -> new ButtonType(entry.getValue(), entry.getKey()))
                .toArray(ButtonType[]::new);
    }

    public static void decorateDialogAs(@NotNull FXContext context, @Nullable Stage owner, Alert dialog,
                                        @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        decorateDialogAs(owner, dialog, (buttonData, button) -> {
                    context.findResource(
                            context.getBaseName(FXDialogs.class, getIconPath(buttonData)), FXConstants.EXT_IMAGES)
                            .map(URL::toExternalForm).map(ImageView::new)
                            .ifPresent(button::setGraphic);
                    if (buttons == null || buttons.isEmpty() || !buttons.containsKey(buttonData)) {
                        button.setText(translateButton(context, buttonData, button.getText()));
                    }
                },
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
    }

    @NotNull
    private static String getTitle(@Nullable Stage owner, @NotNull Supplier<String> titleProvider) {
        return Optional.ofNullable(owner).map(Stage::getTitle).orElseGet(titleProvider);
    }

    private static void setModal(@Nullable Stage owner, @NotNull Dialog<?> dialog) {
        Optional.ofNullable(owner).ifPresent(stage -> {
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
        });
    }

    private static void decorateDialogAs(@Nullable Stage primaryStage, @Nullable Dialog<?> dialog,
                                         @NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator,
                                         @NotNull BiConsumer<Boolean, Hyperlink> detailsButtonDecorator) {
        decorateDialogButtons(dialog, buttonDecorator, detailsButtonDecorator);
        Optional.ofNullable(dialog).map(Dialog::getDialogPane).map(Node::getScene).map(Scene::getWindow)
                .filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(stage -> Objects.nonNull(primaryStage))
                .ifPresent(stage -> doDecorateStageAs(primaryStage, stage));
    }

    @NotNull
    private static String getIconPath(@NotNull ButtonBar.ButtonData buttonData) {
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

    @NotNull
    private static String translateButton(@NotNull FXContext context,
                                          @NotNull ButtonBar.ButtonData buttonData, @NotNull String defaultText) {
        switch (buttonData) {
            case OK_DONE:
                return getString(context, LC_OK, LC_OK);
            case CANCEL_CLOSE:
                return getString(context, LC_CANCEL, LC_CANCEL);
            case YES:
                return getString(context, LC_YES, LC_YES);
            case NO:
                return getString(context, LC_NO, LC_NO);
            case APPLY:
                return getString(context, LC_APPLY, LC_APPLY);
            default:
                return defaultText;
        }
    }

    private static void decorateDialogButtons(@Nullable Dialog<?> dialog,
                                              @NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator,
                                              @NotNull BiConsumer<Boolean, Hyperlink> detailsButtonDecorator) {
        Optional.ofNullable(dialog).ifPresent(window -> {
            DialogPane oldDialogPane = window.getDialogPane();
            window.setDialogPane(new DecoratedDialogPane(buttonDecorator, detailsButtonDecorator));
            window.getDialogPane().getButtonTypes().setAll(oldDialogPane.getButtonTypes());
        });
    }

    private static void doDecorateStageAs(@NotNull Stage primaryStage, @NotNull Stage window) {
        window.setTitle(primaryStage.getTitle());
        window.getIcons().setAll(primaryStage.getIcons());
    }

    @NotNull
    public static Optional<ButtonType> alert(@Nullable Stage owner, @NotNull String message,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return alert(FXContextFactory.currentContext(), owner, message, buttons);
    }

    @NotNull
    public static Optional<ButtonType> alert(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return dialog(context, owner, AlertType.INFORMATION, message,
                () -> getString(context, LC_INFORMATION, LC_INFORMATION), buttons);
    }

    @Nullable
    public static Boolean ask(@Nullable Stage owner, @NotNull String message) {
        return ask(FXContextFactory.currentContext(), owner, message);
    }

    @Nullable
    public static Boolean ask(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        return confirm(context, owner, message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                .map(buttonType -> buttonType == ButtonType.YES).orElse(null);
    }

    @NotNull
    private static Optional<ButtonType> confirm(@NotNull FXContext context,
                                                @Nullable Stage owner, @NotNull String message, ButtonType... buttons) {
        return dialog(context, owner, AlertType.CONFIRMATION, message,
                () -> getString(context, LC_CONFIRMATION, LC_CONFIRMATION), asMap(buttons));
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(@Nullable Stage owner, @NotNull String message) {
        return confirm(FXContextFactory.currentContext(), owner, message);
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        return confirm(context, owner, message, ButtonType.OK, ButtonType.CANCEL)
                .map(buttonType -> buttonType == ButtonType.OK).orElse(Boolean.FALSE);
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(@Nullable Stage owner, @NotNull String message,
                                  @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return confirm(FXContextFactory.currentContext(), owner, message, buttons)
                .map(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE).orElse(false);
    }

    @NotNull
    public static Optional<ButtonType> confirm(@NotNull FXContext context,
                                               @Nullable Stage owner, @NotNull String message,
                                               @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return dialog(context, owner, AlertType.CONFIRMATION, message,
                () -> getString(context, LC_CONFIRMATION, LC_CONFIRMATION), buttons);
    }

    @NotNull
    public static Hyperlink createHyperlink(String text, Consumer<Hyperlink> action) {
        Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setOnAction(event -> action.accept(hyperlink));
        return hyperlink;
    }

    public static <R> void decorateDialog(Dialog<R> dialog,
                                          @NotNull Callback<ButtonType, R> resultConverter,
                                          @Nullable Map<ButtonBar.ButtonData, String> buttons,
                                          @NotNull String title) {
        FXContext context = FXContextFactory.currentContext();
        decorateDialog(dialog, resultConverter, buttons, title,
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
    }

    public static <R> void decorateDialog(Dialog<R> dialog,
                                          @NotNull Callback<ButtonType, R> resultConverter,
                                          @Nullable Map<ButtonBar.ButtonData, String> buttons,
                                          @NotNull String title,
                                          @NotNull BiConsumer<Boolean, Hyperlink> detailsButtonDecorator) {
        decorateDialog(FXContextFactory.currentContext(),
                dialog, resultConverter, buttons, title, detailsButtonDecorator);
    }

    public static <R> void decorateDialog(@NotNull FXContext context, Dialog<R> dialog,
                                          @NotNull Callback<ButtonType, R> resultConverter,
                                          @Nullable Map<ButtonBar.ButtonData, String> buttons,
                                          @NotNull String title,
                                          @NotNull BiConsumer<Boolean, Hyperlink> detailsButtonDecorator) {
        dialog.setTitle(title);
        dialog.setDialogPane(new DecoratedDialogPane((buttonData, button) ->
                context.findResource(context.getBaseName(dialog.getClass(), getIconPath(buttonData)),
                        FXConstants.EXT_IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new)
                        .ifPresent(button::setGraphic),
                detailsButtonDecorator));
        if (buttons == null || buttons.isEmpty()) {
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        } else {
            dialog.getDialogPane().getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        dialog.setResultConverter(resultConverter);
    }

    @NotNull
    public static List<ButtonType> asButtonTypeCollection(@NotNull Map<ButtonBar.ButtonData, String> buttons) {
        return buttons.entrySet().stream()
                .map(entry -> new ButtonType(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    @NotNull
    public static Optional<ButtonType> error(@Nullable Stage owner,
                                             @NotNull String message, @Nullable Exception ex,
                                             @NotNull ButtonType... buttons) {
        return error(FXContextFactory.currentContext(), owner, message, ex, buttons);
    }

    @NotNull
    public static Optional<ButtonType> error(@NotNull FXContext context, @Nullable Stage owner,
                                             @NotNull String message, @Nullable Exception ex,
                                             @NotNull ButtonType... buttons) {
        return error(context, owner, message, ex, asMap(buttons));
    }

    @NotNull
    public static Optional<ButtonType> error(@NotNull FXContext context, @Nullable Stage owner,
                                             @NotNull String message, @Nullable Exception ex,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        if (ex != null) {
            log.info(ex.getLocalizedMessage(), ex);
        }
        Alert dialog = new Alert(AlertType.ERROR);
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(context, owner, "error", "Error"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        if (ex != null) {
            Label label = new Label(ex.getClass().getName());
            String exceptionText = FXUtil.stringStackTrace(ex);
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
        if (buttons != null && !buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    @NotNull
    private static String getTitle(@NotNull FXContext context,
                                   @Nullable Stage owner, @NotNull String key, @NotNull String defaultTitle) {
        return getTitle(owner, () -> getString(context, key, defaultTitle));
    }

    @NotNull
    public static Optional<ButtonType> error(@Nullable Stage owner,
                                             @NotNull String message, @Nullable Exception ex,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return error(FXContextFactory.currentContext(), owner, message, ex, buttons);
    }

    @NotNull
    public static Optional<ButtonType> error(@Nullable Stage owner, @NotNull String message,
                                             @NotNull ButtonType... buttons) {
        return error(FXContextFactory.currentContext(), owner, message, buttons);
    }

    @NotNull
    public static Optional<ButtonType> error(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message,
                                             @NotNull ButtonType... buttons) {
        return error(context, owner, message, asMap(buttons));
    }

    @NotNull
    public static Optional<ButtonType> error(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        Alert dialog = new Alert(AlertType.INFORMATION);
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(context, owner, "error", "Error"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        if (buttons != null && !buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    @NotNull
    public static Optional<ButtonType> error(@Nullable Stage owner, @NotNull String message,
                                             @Nullable Map<ButtonBar.ButtonData, String> buttons) {
        return error(FXContextFactory.currentContext(), owner, message, buttons);
    }

    @Nullable
    public static String prompt(@Nullable Stage owner, @NotNull String message) {
        return prompt(FXContextFactory.currentContext(), owner, message);
    }

    @Nullable
    public static String prompt(@NotNull FXContext context, @Nullable Stage owner, @NotNull String message) {
        return prompt(context, owner, message, "");
    }

    @SuppressWarnings("SameParameterValue")
    @Nullable
    private static String prompt(@NotNull FXContext context,
                                 @Nullable Stage owner, @NotNull String message, @NotNull String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        decorateDialogAs(owner, dialog, (buttonData, button) -> {
                    context.findResource(
                            context.getBaseName(FXDialogs.class, getIconPath(buttonData)), FXConstants.EXT_IMAGES)
                            .map(URL::toExternalForm).map(ImageView::new)
                            .ifPresent(button::setGraphic);
                    button.setText(translateButton(context, buttonData, button.getText()));
                },
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
        dialog.setTitle(getTitle(context, owner, "prompt", "Please enter"));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        return dialog.showAndWait().orElse(null);
    }

    private static final class DecoratedDialogPane extends DialogPane {
        @NotNull
        private final BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator;
        private final BiConsumer<Boolean, Hyperlink> detailsButtonDecorator;

        private DecoratedDialogPane(@NotNull BiConsumer<ButtonBar.ButtonData, Button> buttonDecorator,
                                    @NotNull BiConsumer<Boolean, Hyperlink> detailsButtonDecorator) {
            this.buttonDecorator = buttonDecorator;
            this.detailsButtonDecorator = detailsButtonDecorator;
        }

        @Override
        protected Node createButton(ButtonType buttonType) {
            Node node = super.createButton(buttonType);
            if (node instanceof Button) {
                buttonDecorator.accept(buttonType.getButtonData(), (Button) node);
            }
            return node;
        }

        @Override
        protected Node createDetailsButton() {
            final Hyperlink detailsButton = new Hyperlink();
            final String moreText = "Dialog.detail.button.more"; //$NON-NLS-1$
            final String lessText = "Dialog.detail.button.less"; //$NON-NLS-1$

            InvalidationListener expandedListener = o -> {
                final boolean isExpanded = isExpanded();
                detailsButton.getStyleClass().setAll("details-button", isExpanded ? "less" : "more");
                //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                detailsButton.setText(isExpanded ? lessText : moreText);
                detailsButtonDecorator.accept(isExpanded, detailsButton);
            };
            expandedListener.invalidated(null);
            expandedProperty().addListener(expandedListener);
            detailsButton.setOnAction(ae -> setExpanded(!isExpanded()));
            return detailsButton;
        }
    }
}
