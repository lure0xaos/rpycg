package gargoyle.fx

import gargoyle.fx.icons.Icon
import gargoyle.fx.log.FxLog
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.DialogPane
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputDialog
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback

@Suppress("SameParameterValue", "unused", "MemberVisibilityCanBePrivate")
internal object FxDialogs {

    private class DecoratedDialogPane(
        private val buttonDecorator: (ButtonData, Button) -> Unit,
        private val detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) : DialogPane() {
        override fun createButton(buttonType: ButtonType): Node =
            super.createButton(buttonType)
                .also { if (it is Button) buttonDecorator(buttonType.buttonData, it) }

        override fun createDetailsButton(): Node {
            val detailsButton = Hyperlink()
            val expandedListener = InvalidationListener {
                detailsButton.styleClass.setAll(
                    CLASS_DETAILS_BUTTON,
                    if (this.isExpanded) CLASS_DETAILS_BUTTON_LESS else CLASS_DETAILS_BUTTON_MORE
                )
                detailsButton.text = if (this.isExpanded) LESS_TEXT else MORE_TEXT
                detailsButtonDecorator(this.isExpanded, detailsButton)
            }
            expandedListener.invalidated(null)
            expandedProperty().addListener(expandedListener)
            detailsButton.onAction = EventHandler { isExpanded = !isExpanded }
            return detailsButton
        }

        companion object {
            private const val CLASS_DETAILS_BUTTON = "details-button"
            private const val CLASS_DETAILS_BUTTON_LESS = "less"
            private const val CLASS_DETAILS_BUTTON_MORE = "more"
            private const val LESS_TEXT = "Dialog.detail.button.less"
            private const val MORE_TEXT = "Dialog.detail.button.more"
        }
    }

    private const val LC_APPLY = "apply"
    private const val LC_CANCEL = "cancel"
    private const val LC_CONFIRMATION = "confirmation"
    private const val LC_ERROR = "error"
    private const val LC_INFORMATION = "information"
    private const val LC_LESS = "less"
    private const val LC_MORE = "more"
    private const val LC_NO = "no"
    private const val LC_OK = "ok"
    private const val LC_PROMPT = "prompt"
    private const val LC_YES = "yes"
    private const val MSG_ERROR = "Error"
    private const val MSG_PROMPT = "Please enter"

    fun alert(context: FxContext, owner: Stage, message: String, vararg buttons: ButtonType): ButtonType? =
        dialog(
            context,
            owner,
            AlertType.INFORMATION,
            message,
            { getString(context, LC_INFORMATION, LC_INFORMATION) },
            asMap(buttons)
        )

    fun alert(context: FxContext, owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? {
        return dialog(
            context,
            owner,
            AlertType.INFORMATION,
            message,
            { getString(context, LC_INFORMATION, LC_INFORMATION) },
            buttons
        )
    }

    fun asButtonTypeArray(buttons: Map<ButtonData, String>): Array<ButtonType> =
        buttons.map { (key, value) -> ButtonType(value, key) }.toTypedArray()

    fun asButtonTypeCollection(buttons: Map<ButtonData, String>): List<ButtonType> =
        buttons.map { (key, value) -> ButtonType(value, key) }

    fun ask(context: FxContext, owner: Stage, message: String): Boolean? {
        return confirm(
            context,
            owner,
            message,
            ButtonType.YES,
            ButtonType.NO,
            ButtonType.CANCEL
        )?.let { it == ButtonType.YES }
    }

    fun confirm(context: FxContext, owner: Stage, message: String): Boolean {
        return confirm(
            context,
            owner,
            message,
            ButtonType.OK,
            ButtonType.CANCEL
        )?.let { it == ButtonType.OK } ?: false
    }

    fun confirm(context: FxContext, owner: Stage, message: String, buttons: Map<ButtonData, String>): Boolean =
        confirmExt(
            context,
            owner,
            message,
            buttons
        )?.let { ButtonData.OK_DONE == it.buttonData } ?: false

    fun confirmExt(context: FxContext, owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? =
        dialog(
            context,
            owner,
            AlertType.CONFIRMATION,
            message,
            { getString(context, LC_CONFIRMATION, LC_CONFIRMATION) },
            buttons
        )

    fun createHyperlink(text: String, action: (Hyperlink) -> Unit): Hyperlink =
        Hyperlink(text).also { hyperlink -> hyperlink.onAction = EventHandler { action(hyperlink) } }

    fun <R> decorateDialog(
        context: FxContext,
        dialog: Dialog<R>,
        resultConverter: (ButtonType) -> R?,
        buttons: Map<ButtonData, String>,
        title: String
    ) =
        decorateDialog(context, dialog, resultConverter, buttons, title) { expanded: Boolean, button: Hyperlink ->
            button.text = if (expanded) getString(context, LC_LESS, LC_LESS) else getString(context, LC_MORE, LC_MORE)
        }

    fun <R : Any?> decorateDialog(
        context: FxContext,
        dialog: Dialog<R>,
        resultConverter: (ButtonType) -> R?,
        buttons: Map<ButtonData, String>,
        title: String,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        dialog.title = title
        dialog.dialogPane = DecoratedDialogPane(
            { buttonData: ButtonData, button: Button ->
                Icon.find(buttonData)
                    .findIcon(context)?.toExternalForm()
                    ?.let { ImageView(it) }
                    ?.also { button.graphic = it }
            }, detailsButtonDecorator
        )
        dialog.dialogPane.buttonTypes.setAll(
            if (buttons.isEmpty()) listOf(ButtonType.OK, ButtonType.CANCEL)
            else asButtonTypeCollection(buttons)
        )
        dialog.resultConverter = Callback(resultConverter)
    }

    fun decorateDialogAs(context: FxContext, owner: Stage, dialog: Alert, buttons: Map<ButtonData, String>) =
        decorateDialogAs(owner, dialog, { buttonData: ButtonData, button: Button ->
            Icon.find(buttonData).findIcon(context)?.toExternalForm()
                ?.let { ImageView(it) }
                ?.also { button.graphic = it }
            if (buttons.isEmpty() || !buttons.containsKey(buttonData)) {
                button.text = translateButton(context, buttonData, button.text)
            }
        }) { expanded: Boolean, button: Hyperlink ->
            button.text = if (expanded) getString(context, LC_LESS, LC_LESS) else getString(context, LC_MORE, LC_MORE)
        }

    fun dialog(
        context: FxContext,
        owner: Stage,
        type: AlertType,
        message: String,
        titleProvider: () -> String,
        buttons: Map<ButtonData, String>
    ): ButtonType? {
        val dialog = if (buttons.isEmpty()) Alert(type, message) else Alert(type, message, *asButtonTypeArray(buttons))
        decorateDialogAs(context, owner, dialog, buttons)
        dialog.title = getTitle(owner, titleProvider)
        dialog.headerText = null
        dialog.contentText = message
        setModal(owner, dialog)
        return dialog.showAndWait().orElse(null)
    }

    fun error(
        context: FxContext,
        owner: Stage,
        message: String,
        ex: Exception?,
        vararg buttons: ButtonType
    ): ButtonType? =
        error(context, owner, message, ex, asMap(buttons))

    fun error(
        context: FxContext,
        owner: Stage,
        message: String,
        ex: Exception?,
        buttons: Map<ButtonData, String>
    ): ButtonType? {
        if (null != ex) FxLog.info(ex, ex.localizedMessage)
        val dialog = Alert(AlertType.ERROR)
        decorateDialogAs(context, owner, dialog, buttons)
        dialog.title = getTitle(context, owner, LC_ERROR, MSG_ERROR)
        dialog.headerText = null
        dialog.contentText = message
        if (null != ex) {
            val textArea = TextArea(ex.stackTraceToString())
            textArea.isEditable = false
            textArea.isWrapText = true
            textArea.maxWidth = Double.MAX_VALUE
            textArea.maxHeight = Double.MAX_VALUE
            GridPane.setVgrow(textArea, Priority.ALWAYS)
            GridPane.setHgrow(textArea, Priority.ALWAYS)
            val expContent = GridPane()
            expContent.maxWidth = Double.MAX_VALUE
            expContent.add(Label(ex.javaClass.name), 0, 0)
            expContent.add(textArea, 0, 1)
            dialog.dialogPane.expandableContent = expContent
        }
        if (buttons.isNotEmpty()) dialog.buttonTypes.setAll(asButtonTypeCollection(buttons))
        setModal(owner, dialog)
        return dialog.showAndWait().orElse(null)
    }

    fun error(context: FxContext, owner: Stage, message: String, vararg buttons: ButtonType): ButtonType? =
        error(context, owner, message, asMap(buttons))

    fun error(context: FxContext, owner: Stage, message: String, buttons: Map<ButtonData, String>): ButtonType? {
        val dialog = Alert(AlertType.INFORMATION)
        decorateDialogAs(context, owner, dialog, buttons)
        dialog.title = getTitle(context, owner, LC_ERROR, MSG_ERROR)
        dialog.headerText = null
        dialog.contentText = message
        if (buttons.isNotEmpty()) dialog.buttonTypes.setAll(asButtonTypeCollection(buttons))
        setModal(owner, dialog)
        return dialog.showAndWait().orElse(null)
    }

    fun prompt(context: FxContext, owner: Stage, message: String): String =
        prompt(context, owner, message, "")

    private fun asMap(buttons: Array<out ButtonType>): Map<ButtonData, String> =
        buttons.associate { it.buttonData to it.text }

    private fun confirm(context: FxContext, owner: Stage, message: String, vararg buttons: ButtonType): ButtonType? =
        dialog(
            context,
            owner,
            AlertType.CONFIRMATION,
            message,
            { getString(context, LC_CONFIRMATION, LC_CONFIRMATION) },
            asMap(buttons)
        )

    private fun decorateDialogAs(
        primaryStage: Stage,
        dialog: Dialog<*>,
        buttonDecorator: (ButtonData, Button) -> Unit,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        decorateDialogButtons(dialog, buttonDecorator, detailsButtonDecorator)
        if (dialog.dialogPane.scene?.window != null)
            doDecorateStageAs(primaryStage, dialog.dialogPane.scene?.window as Stage)
    }

    private fun decorateDialogButtons(
        dialog: Dialog<*>,
        buttonDecorator: (ButtonData, Button) -> Unit,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        val oldDialogPane = dialog.dialogPane
        dialog.dialogPane = DecoratedDialogPane(buttonDecorator, detailsButtonDecorator)
        dialog.dialogPane.buttonTypes.setAll(oldDialogPane.buttonTypes)
    }

    private fun doDecorateStageAs(primaryStage: Stage, window: Stage) {
        window.title = primaryStage.title
        window.icons.setAll(primaryStage.icons)
    }

    private fun getString(context: FxContext, key: String, defaultTitle: String): String =
        FxUtil.message(FxDialogs::class, context.locale, { defaultTitle }, key, mapOf<String, Any>())

    private fun getTitle(owner: Stage, titleProvider: () -> String): String =
        owner.title ?: titleProvider()

    private fun getTitle(context: FxContext, owner: Stage, key: String, defaultTitle: String): String =
        getTitle(owner) { getString(context, key, defaultTitle) }

    private fun prompt(context: FxContext, owner: Stage, message: String, defaultValue: String): String {
        val dialog = TextInputDialog(defaultValue)
        decorateDialogAs(owner, dialog, { buttonData: ButtonData, button: Button ->
            Icon.find(buttonData)
                .findIcon(context)?.toExternalForm()
                ?.let { ImageView(it) }
                ?.also { button.graphic = it }
            button.text = translateButton(context, buttonData, button.text)
        }) { expanded: Boolean, button: Hyperlink ->
            button.text = if (expanded) getString(context, LC_LESS, LC_LESS) else getString(context, LC_MORE, LC_MORE)
        }
        dialog.title = getTitle(context, owner, LC_PROMPT, MSG_PROMPT)
        dialog.headerText = null
        dialog.contentText = message
        setModal(owner, dialog)
        return dialog.showAndWait().orElse(null)
    }

    private fun setModal(owner: Stage, dialog: Dialog<*>) {
        dialog.initOwner(owner)
        dialog.initModality(Modality.WINDOW_MODAL)
    }

    private fun translateButton(context: FxContext, buttonData: ButtonData, defaultText: String): String =
        when (buttonData) {
            ButtonData.OK_DONE -> getString(context, LC_OK, LC_OK)
            ButtonData.CANCEL_CLOSE -> getString(context, LC_CANCEL, LC_CANCEL)
            ButtonData.YES -> getString(context, LC_YES, LC_YES)
            ButtonData.NO -> getString(context, LC_NO, LC_NO)
            ButtonData.APPLY -> getString(context, LC_APPLY, LC_APPLY)
            else -> defaultText
        }

}
