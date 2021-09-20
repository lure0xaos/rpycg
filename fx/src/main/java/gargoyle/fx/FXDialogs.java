package gargoyle.fx;

import gargoyle.fx.icons.Icon;
import gargoyle.fx.log.FXLog;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class FXDialogs {
    private static final String LC_APPLY = "apply";
    private static final String LC_CANCEL = "cancel";
    private static final String LC_CONFIRMATION = "confirmation";
    private static final String LC_ERROR = "error";
    private static final String LC_INFORMATION = "information";
    private static final String LC_LESS = "less";
    private static final String LC_MORE = "more";
    private static final String LC_NO = "no";
    private static final String LC_OK = "ok";
    private static final String LC_PROMPT = "prompt";
    private static final String LC_YES = "yes";
    private static final String MSG_ERROR = "Error";
    private static final String MSG_PROMPT = "Please enter";

    private FXDialogs() {
        throw new IllegalStateException(FXDialogs.class.getName());
    }

    public static Optional<ButtonType> alert(final FXContext context, final Stage owner, final String message,
                                             final ButtonType... buttons) {
        return dialog(context, owner, Alert.AlertType.INFORMATION, message,
                () -> getString(context, LC_INFORMATION, LC_INFORMATION), asMap(buttons));
    }

    public static Optional<ButtonType> alert(final FXContext context, final Stage owner, final String message,
                                             final Map<ButtonBar.ButtonData, String> buttons) {
        return dialog(context, owner, Alert.AlertType.INFORMATION, message,
                () -> getString(context, LC_INFORMATION, LC_INFORMATION), buttons);
    }

    public static ButtonType[] asButtonTypeArray(final Map<ButtonBar.ButtonData, String> buttons) {
        return buttons.entrySet().stream()
                .map(entry -> new ButtonType(entry.getValue(), entry.getKey()))
                .toArray(ButtonType[]::new);
    }

    public static List<ButtonType> asButtonTypeCollection(final Map<ButtonBar.ButtonData, String> buttons) {
        return buttons.entrySet().stream()
                .map(entry -> new ButtonType(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    public static Boolean ask(final FXContext context, final Stage owner, final String message) {
        return confirm(context, owner, message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                .map(buttonType -> buttonType == ButtonType.YES).orElse(null);
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(final FXContext context, final Stage owner, final String message) {
        return confirm(context, owner, message, ButtonType.OK, ButtonType.CANCEL)
                .map(buttonType -> buttonType == ButtonType.OK).orElse(Boolean.FALSE);
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean confirm(final FXContext context, final Stage owner, final String message,
                                  final Map<ButtonBar.ButtonData, String> buttons) {
        return confirmExt(context, owner, message, buttons)
                .map(buttonType -> ButtonBar.ButtonData.OK_DONE == buttonType.getButtonData()).orElse(false);
    }

    public static Optional<ButtonType> confirmExt(final FXContext context,
                                                  final Stage owner, final String message,
                                                  final Map<ButtonBar.ButtonData, String> buttons) {
        return dialog(context, owner, Alert.AlertType.CONFIRMATION, message,
                () -> getString(context, LC_CONFIRMATION, LC_CONFIRMATION), buttons);
    }

    public static Hyperlink createHyperlink(final String text, final Consumer<? super Hyperlink> action) {
        final Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setOnAction(event -> action.accept(hyperlink));
        return hyperlink;
    }

    public static <R> void decorateDialog(final FXContext context, final Dialog<R> dialog,
                                          final Callback<ButtonType, R> resultConverter,
                                          final Map<ButtonBar.ButtonData, String> buttons,
                                          final String title) {
        decorateDialog(context, dialog, resultConverter, buttons, title,
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
    }

    public static <R> void decorateDialog(final FXContext context, final Dialog<R> dialog,
                                          final Callback<ButtonType, R> resultConverter,
                                          final Map<ButtonBar.ButtonData, String> buttons,
                                          final String title,
                                          final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator) {
        dialog.setTitle(title);
        dialog.setDialogPane(new DecoratedDialogPane((buttonData, button) -> Icon.find(buttonData).findIcon(context)
                .map(URL::toExternalForm).map(ImageView::new)
                .ifPresent(button::setGraphic),
                detailsButtonDecorator));
        if (null == buttons || buttons.isEmpty()) {
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        } else {
            dialog.getDialogPane().getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        dialog.setResultConverter(resultConverter);
    }

    public static void decorateDialogAs(final FXContext context, final Stage owner, final Alert dialog,
                                        final Map<ButtonBar.ButtonData, String> buttons) {
        decorateDialogAs(owner, dialog, (buttonData, button) -> {
                    Icon.find(buttonData).findIcon(context)
                            .map(URL::toExternalForm).map(ImageView::new)
                            .ifPresent(button::setGraphic);
                    if (null == buttons || buttons.isEmpty() || !buttons.containsKey(buttonData)) {
                        button.setText(translateButton(context, buttonData, button.getText()));
                    }
                },
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
    }

    public static Optional<ButtonType> dialog(final FXContext context,
                                              final Stage owner,
                                              final Alert.AlertType type,
                                              final String message,
                                              final Supplier<String> titleProvider,
                                              final Map<ButtonBar.ButtonData, String> buttons) {
        final Alert dialog = null == buttons || buttons.isEmpty() ?
                new Alert(type, message) :
                new Alert(type, message, asButtonTypeArray(buttons));
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(owner, titleProvider));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> error(final FXContext context, final Stage owner,
                                             final String message, final Exception ex,
                                             final ButtonType... buttons) {
        return error(context, owner, message, ex, asMap(buttons));
    }

    public static Optional<ButtonType> error(final FXContext context, final Stage owner,
                                             final String message, final Exception ex,
                                             final Map<ButtonBar.ButtonData, String> buttons) {
        if (null != ex) {
            FXLog.info(ex, ex.getLocalizedMessage());
        }
        final Alert dialog = new Alert(Alert.AlertType.ERROR);
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(context, owner, LC_ERROR, MSG_ERROR));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        if (null != ex) {
            final Label label = new Label(ex.getClass().getName());
            final String exceptionText = FXUtil.stringStackTrace(ex);
            final TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            final GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
            dialog.getDialogPane().setExpandableContent(expContent);
        }
        if (null != buttons && !buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> error(final FXContext context, final Stage owner, final String message,
                                             final ButtonType... buttons) {
        return error(context, owner, message, asMap(buttons));
    }

    public static Optional<ButtonType> error(final FXContext context, final Stage owner, final String message,
                                             final Map<ButtonBar.ButtonData, String> buttons) {
        final Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        decorateDialogAs(context, owner, dialog, buttons);
        dialog.setTitle(getTitle(context, owner, LC_ERROR, MSG_ERROR));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        if (null != buttons && !buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(asButtonTypeCollection(buttons));
        }
        setModal(owner, dialog);
        return dialog.showAndWait();
    }

    public static String prompt(final FXContext context, final Stage owner, final String message) {
        return prompt(context, owner, message, "");
    }

    private static Map<ButtonBar.ButtonData, String> asMap(final ButtonType[] buttons) {
        return Arrays.stream(buttons).collect(Collectors.toMap(ButtonType::getButtonData, ButtonType::getText));
    }

    private static Optional<ButtonType> confirm(final FXContext context,
                                                final Stage owner, final String message, final ButtonType... buttons) {
        return dialog(context, owner, Alert.AlertType.CONFIRMATION, message,
                () -> getString(context, LC_CONFIRMATION, LC_CONFIRMATION), asMap(buttons));
    }

    private static void decorateDialogAs(final Stage primaryStage, final Dialog<?> dialog,
                                         final BiConsumer<? super ButtonBar.ButtonData, ? super Button> buttonDecorator,
                                         final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator) {
        decorateDialogButtons(dialog, buttonDecorator, detailsButtonDecorator);
        Optional.ofNullable(dialog).map(Dialog::getDialogPane).map(Node::getScene).map(Scene::getWindow)
                .filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(stage -> Objects.nonNull(primaryStage))
                .ifPresent(stage -> doDecorateStageAs(primaryStage, stage));
    }

    private static void decorateDialogButtons(final Dialog<?> dialog,
                                              final BiConsumer<? super ButtonBar.ButtonData, ? super Button> buttonDecorator,
                                              final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator) {
        Optional.ofNullable(dialog).ifPresent(window -> {
            final DialogPane oldDialogPane = window.getDialogPane();
            window.setDialogPane(new DecoratedDialogPane(buttonDecorator, detailsButtonDecorator));
            window.getDialogPane().getButtonTypes().setAll(oldDialogPane.getButtonTypes());
        });
    }

    private static void doDecorateStageAs(final Stage primaryStage, final Stage window) {
        window.setTitle(primaryStage.getTitle());
        window.getIcons().setAll(primaryStage.getIcons());
    }

    private static String getString(final FXContext context,
                                    final String key,
                                    final String defaultTitle) {
        return FXUtil.message(FXDialogs.class, context.getLocale(), () -> defaultTitle, key, Map.of());
    }

    private static String getTitle(final Stage owner, final Supplier<String> titleProvider) {
        return Optional.ofNullable(owner).map(Stage::getTitle).orElseGet(titleProvider);
    }

    private static String getTitle(final FXContext context,
                                   final Stage owner, final String key, final String defaultTitle) {
        return getTitle(owner, () -> getString(context, key, defaultTitle));
    }

    @SuppressWarnings("SameParameterValue")
    private static String prompt(final FXContext context,
                                 final Stage owner, final String message, final String defaultValue) {
        final TextInputDialog dialog = new TextInputDialog(defaultValue);
        decorateDialogAs(owner, dialog, (buttonData, button) -> {
                    Icon.find(buttonData).findIcon(context).map(URL::toExternalForm).map(ImageView::new)
                            .ifPresent(button::setGraphic);
                    button.setText(translateButton(context, buttonData, button.getText()));
                },
                (isExpanded, detailsButton) -> detailsButton.setText(isExpanded ?
                        getString(context, LC_LESS, LC_LESS) : getString(context, LC_MORE, LC_MORE)));
        dialog.setTitle(getTitle(context, owner, LC_PROMPT, MSG_PROMPT));
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setModal(owner, dialog);
        return dialog.showAndWait().orElse(null);
    }

    private static void setModal(final Stage owner, final Dialog<?> dialog) {
        Optional.ofNullable(owner).ifPresent(stage -> {
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
        });
    }

    private static String translateButton(final FXContext context,
                                          final ButtonBar.ButtonData buttonData, final String defaultText) {
        return switch (buttonData) {
            case OK_DONE -> getString(context, LC_OK, LC_OK);
            case CANCEL_CLOSE -> getString(context, LC_CANCEL, LC_CANCEL);
            case YES -> getString(context, LC_YES, LC_YES);
            case NO -> getString(context, LC_NO, LC_NO);
            case APPLY -> getString(context, LC_APPLY, LC_APPLY);
            default -> defaultText;
        };
    }

    private static final class DecoratedDialogPane extends DialogPane {
        private static final String CLASS_DETAILS_BUTTON = "details-button";
        private static final String CLASS_DETAILS_BUTTON_LESS = "less";
        private static final String CLASS_DETAILS_BUTTON_MORE = "more";
        private static final String LESS_TEXT = "Dialog.detail.button.less";
        private static final String MORE_TEXT = "Dialog.detail.button.more";
        private final BiConsumer<? super ButtonBar.ButtonData, ? super Button> buttonDecorator;
        private final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator;

        private DecoratedDialogPane(final BiConsumer<? super ButtonBar.ButtonData, ? super Button> buttonDecorator,
                                    final BiConsumer<? super Boolean, ? super Hyperlink> detailsButtonDecorator) {
            this.buttonDecorator = buttonDecorator;
            this.detailsButtonDecorator = detailsButtonDecorator;
        }

        @Override
        protected Node createButton(final ButtonType buttonType) {
            final Node node = super.createButton(buttonType);
            if (node instanceof Button) {
                buttonDecorator.accept(buttonType.getButtonData(), (Button) node);
            }
            return node;
        }

        @Override
        protected Node createDetailsButton() {
            final Hyperlink detailsButton = new Hyperlink();
            final InvalidationListener expandedListener = observable -> {
                final boolean isExpanded = isExpanded();
                detailsButton.getStyleClass().setAll(CLASS_DETAILS_BUTTON,
                        isExpanded ? CLASS_DETAILS_BUTTON_LESS : CLASS_DETAILS_BUTTON_MORE);
                detailsButton.setText(isExpanded ? LESS_TEXT : MORE_TEXT);
                detailsButtonDecorator.accept(isExpanded, detailsButton);
            };
            expandedListener.invalidated(null);
            expandedProperty().addListener(expandedListener);
            detailsButton.setOnAction(ae -> setExpanded(!isExpanded()));
            return detailsButton;
        }
    }
}
