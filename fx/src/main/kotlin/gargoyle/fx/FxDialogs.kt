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

@Suppress("SameParameterValue", "MemberVisibilityCanBePrivate")
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

    fun alert(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Array<ButtonType> = arrayOf()
    ): ButtonType? =
        dialog(
            message,
            AlertType.INFORMATION,
            context,
            owner,
            { getString(LC_INFORMATION, LC_INFORMATION, context) },
            buttons.associate { it.buttonData to it.text }
        )

    fun alert(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        dialog(
            message,
            AlertType.INFORMATION,
            context,
            owner,
            { getString(LC_INFORMATION, LC_INFORMATION, context) },
            buttons
        )

    fun ask(message: String, context: FxContext = FxContext.current, owner: Stage = context.stage): Boolean? =
        confirm(
            message,
            context,
            owner,
            arrayOf(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        )?.let { it == ButtonType.YES }

    fun confirm(message: String, context: FxContext = FxContext.current, owner: Stage = context.stage): Boolean =
        confirm(
            message,
            context,
            owner,
            arrayOf(ButtonType.OK, ButtonType.CANCEL)
        )?.let { it == ButtonType.OK } ?: false

    fun confirm(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): Boolean =
        confirmExt(
            message,
            context,
            owner,
            buttons
        )?.let { ButtonData.OK_DONE == it.buttonData } ?: false

    fun confirmExt(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        dialog(
            message,
            AlertType.CONFIRMATION,
            context,
            owner,
            { getString(LC_CONFIRMATION, LC_CONFIRMATION, context) },
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
        decorateDialog(dialog, resultConverter, title, context, buttons) { expanded: Boolean, button: Hyperlink ->
            button.text = if (expanded) getString(LC_LESS, LC_LESS, context) else getString(LC_MORE, LC_MORE, context)
        }

    fun <R : Any?> decorateDialog(
        dialog: Dialog<R>,
        resultConverter: (ButtonType) -> R?,
        title: String = "",
        context: FxContext = FxContext.current,
        buttons: Map<ButtonData, String> = mapOf(),
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        with(dialog) {
            this.title = title
            dialogPane = DecoratedDialogPane(
                { buttonData: ButtonData, button: Button ->
                    Icon.find(buttonData)
                        .findIcon(context)?.toExternalForm()
                        ?.let { ImageView(it) }
                        ?.also { button.graphic = it }
                }, detailsButtonDecorator
            )
            dialogPane.buttonTypes.setAll(
                if (buttons.isEmpty()) listOf(ButtonType.OK, ButtonType.CANCEL)
                else buttons.map { (key, value) -> ButtonType(value, key) }
            )
            this.resultConverter = Callback(resultConverter)
        }
    }

    fun decorateDialogAs(
        dialog: Alert,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ) =
        decorateDialogAs(dialog, owner, { buttonData: ButtonData, button: Button ->
            Icon.find(buttonData).findIcon(context)?.toExternalForm()
                ?.let { ImageView(it) }
                ?.let { button.graphic = it }
            if (buttons.isEmpty() || !buttons.containsKey(buttonData)) {
                button.text = translateButton(buttonData, button.text, context)
            }
        }) { expanded: Boolean, button: Hyperlink ->
            button.text = if (expanded) getString(LC_LESS, LC_LESS, context) else getString(LC_MORE, LC_MORE, context)
        }

    fun dialog(
        message: String,
        type: AlertType = AlertType.INFORMATION,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        titleProvider: () -> String,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        with(
            if (buttons.isEmpty()) Alert(type, message) else Alert(
                type,
                message,
                *buttons.map { (key, value) -> ButtonType(value, key) }.toTypedArray()
            )
        ) {
            decorateDialogAs(this, context, owner, buttons)
            title = getTitle(owner, titleProvider)
            headerText = null
            contentText = message
            setModal(owner, this)
            this.showAndWait()
        }.orElse(null)

    fun error(
        message: String,
        ex: Exception? = null,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Array<ButtonType> = arrayOf()
    ): ButtonType? =
        error(message, ex, context, owner, buttons.associate { it.buttonData to it.text })

    fun error(
        message: String,
        ex: Exception? = null,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? {
        ex?.let { FxLog.info(ex.localizedMessage, ex) }
        with(Alert(AlertType.ERROR)) {
            decorateDialogAs(this, context, owner, buttons)
            title = getTitle(LC_ERROR, MSG_ERROR, context, owner)
            headerText = null
            contentText = message
            ex?.let {
                with(TextArea(ex.stackTraceToString())) {
                    isEditable = false
                    isWrapText = true
                    maxWidth = Double.MAX_VALUE
                    maxHeight = Double.MAX_VALUE
                    GridPane.setVgrow(this, Priority.ALWAYS)
                    GridPane.setHgrow(this, Priority.ALWAYS)
                    GridPane().also {
                        with(it) {
                            maxWidth = Double.MAX_VALUE
                            add(Label(ex.javaClass.name), 0, 0)
                            add(this, 0, 1)
                        }
                    }.let { dialogPane.expandableContent = it }
                }
            }
            if (buttons.isNotEmpty()) buttonTypes.setAll(buttons.map { (key, value) -> ButtonType(value, key) })
            setModal(owner, this)
            return showAndWait().orElse(null)
        }
    }

    fun error(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Array<ButtonType> = arrayOf()
    ): ButtonType? =
        error(message, context, owner, buttons.associate { it.buttonData to it.text })

    fun error(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Map<ButtonData, String> = mapOf()
    ): ButtonType? =
        with(Alert(AlertType.INFORMATION)) {
            decorateDialogAs(this, context, owner, buttons)
            title = getTitle(LC_ERROR, MSG_ERROR, context, owner)
            headerText = null
            contentText = message
            if (buttons.isNotEmpty()) buttonTypes.setAll(buttons.map { (key, value) -> ButtonType(value, key) })
            setModal(owner, this)
            showAndWait()
        }.orElse(null)

    fun prompt(message: String, context: FxContext = FxContext.current, owner: Stage = context.stage): String =
        prompt(message, "", context, owner)

    private fun confirm(
        message: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage,
        buttons: Array<ButtonType> = arrayOf()
    ): ButtonType? =
        dialog(
            message,
            AlertType.CONFIRMATION,
            context,
            owner,
            { getString(LC_CONFIRMATION, LC_CONFIRMATION, context) },
            buttons.associate { it.buttonData to it.text }
        )

    private fun decorateDialogAs(
        dialog: Dialog<*>,
        primaryStage: Stage = FxContext.current.stage,
        buttonDecorator: (ButtonData, Button) -> Unit,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        decorateDialogButtons(dialog, buttonDecorator, detailsButtonDecorator)
        (dialog.dialogPane.scene?.window)?.let { doDecorateStageAs(primaryStage, it as Stage) }
    }

    private fun decorateDialogButtons(
        dialog: Dialog<*>,
        buttonDecorator: (ButtonData, Button) -> Unit,
        detailsButtonDecorator: (Boolean, Hyperlink) -> Unit
    ) {
        dialog.dialogPane.let {
            with(dialog) {
                dialogPane = DecoratedDialogPane(buttonDecorator, detailsButtonDecorator)
                dialogPane.buttonTypes.setAll(it.buttonTypes)
            }
        }
    }

    private fun doDecorateStageAs(primaryStage: Stage = FxContext.current.stage, window: Stage) {
        with(window) {
            title = primaryStage.title
            icons.setAll(primaryStage.icons)
        }
    }

    private fun getString(key: String, defaultTitle: String = key, context: FxContext = FxContext.current): String =
        FxUtil.message(FxDialogs::class, context.locale, { defaultTitle }, key, mapOf())

    private fun getTitle(owner: Stage, titleProvider: () -> String): String =
        owner.title ?: titleProvider()

    private fun getTitle(
        key: String,
        defaultTitle: String = key,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage
    ): String =
        getTitle(owner) { getString(key, defaultTitle, context) }

    private fun prompt(
        message: String,
        defaultValue: String,
        context: FxContext = FxContext.current,
        owner: Stage = context.stage
    ): String =
        with(TextInputDialog(defaultValue)) {
            decorateDialogAs(this, owner, { buttonData: ButtonData, button: Button ->
                Icon.find(buttonData)
                    .findIcon(context)?.toExternalForm()
                    ?.let { ImageView(it) }
                    ?.let { button.graphic = it }
                button.text = translateButton(buttonData, button.text, context)
            }) { expanded: Boolean, button: Hyperlink ->
                button.text =
                    if (expanded) getString(LC_LESS, LC_LESS, context) else getString(LC_MORE, LC_MORE, context)
            }
            title = getTitle(LC_PROMPT, MSG_PROMPT, context, owner)
            headerText = null
            contentText = message
            setModal(owner, this)
            showAndWait()
        }.orElse(null)

    private fun setModal(owner: Stage, dialog: Dialog<*>) =
        with(dialog) {
            initOwner(owner)
            initModality(Modality.WINDOW_MODAL)
        }

    private fun translateButton(
        buttonData: ButtonData,
        defaultText: String = "",
        context: FxContext = FxContext.current
    ): String =
        when (buttonData) {
            ButtonData.OK_DONE -> getString(LC_OK, LC_OK, context)
            ButtonData.CANCEL_CLOSE -> getString(LC_CANCEL, LC_CANCEL, context)
            ButtonData.YES -> getString(LC_YES, LC_YES, context)
            ButtonData.NO -> getString(LC_NO, LC_NO, context)
            ButtonData.APPLY -> getString(LC_APPLY, LC_APPLY, context)
            else -> defaultText
        }

}
