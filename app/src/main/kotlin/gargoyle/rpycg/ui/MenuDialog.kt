package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.fx.FxRun.runLater
import gargoyle.rpycg.ex.AppUserException
import gargoyle.rpycg.service.Validator
import gargoyle.rpycg.ui.model.DisplayItem
import gargoyle.rpycg.util.Check.isIdentifier
import gargoyle.rpycg.util.Check.isText
import gargoyle.rpycg.util.Classes.classAdd
import gargoyle.rpycg.util.Classes.classRemove
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import javafx.scene.control.Control
import javafx.scene.control.Dialog
import javafx.scene.control.DialogEvent
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import java.net.URL
import java.util.ResourceBundle

@Suppress("unused")
class MenuDialog : Dialog<DisplayItem>(), Initializable {
    private val displayItem: SimpleObjectProperty<DisplayItem> = SimpleObjectProperty(DisplayItem.createMenu("", ""))
    private val edit: SimpleBooleanProperty = SimpleBooleanProperty(false)
    private val known: ObservableList<String> = FXCollections.observableArrayList()

    @FXML
    private lateinit var label: TextField

    @FXML
    private lateinit var name: TextField
    private lateinit var validator: Validator

    init {
        FxContext.current.loadDialog<MenuDialog, Parent>(this) ?: throw AppUserException("No view {MenuDialog}")
    }

    fun displayItemProperty(): SimpleObjectProperty<DisplayItem> =
        displayItem

    fun editProperty(): SimpleBooleanProperty =
        edit

    fun getDisplayItem(): DisplayItem =
        displayItem.value

    fun setDisplayItem(displayItem: DisplayItem) {
        this.displayItem.value = displayItem
    }

    fun getKnown(): List<String> =
        known

    fun setKnown(value: Collection<String>) {
        known.setAll(value)
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        FxContext.current.decorateDialog(
            this, {
                if (it?.buttonData?.isCancelButton == true) null
                else DisplayItem.createMenu(label.text, name.text)
            }, mapOf(
                ButtonData.OK_DONE to createOk(resources, edit.value),
                ButtonData.CANCEL_CLOSE to resources.getString(LC_CANCEL)
            ), createTitle(resources, edit.value)
        )
        edit.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            title = createTitle(resources, newValue)
            dialogPane.buttonTypes.replaceAll { buttonType: ButtonType ->
                if (buttonType.buttonData.isDefaultButton) ButtonType(
                    createOk(resources, newValue), ButtonData.OK_DONE
                ) else buttonType
            }
            onShow()
        }
        displayItem.addListener { _: ObservableValue<out DisplayItem>, _: DisplayItem?, newValue: DisplayItem ->
            edit.value = newValue.name.value.isNotBlank()
            label.text = newValue.label.value
            name.text = newValue.name.value
        }
        label.textProperty().addListener { _: ObservableValue<out String>, _: String?, newValue: String ->
            displayItem.value!!.label.value = newValue
        }
        name.textProperty().addListener { _: ObservableValue<out String>, _: String?, newValue: String ->
            displayItem.value!!.name.value = newValue
        }
        validator = Validator()
        validator.addValidator(name.textProperty(),
            { s: String -> if (s.isBlank()) resources.getString(LC_ERROR_EMPTY) else null }) { errors: Set<String> ->
            decorateError(
                name,
                errors
            )
        }
        validator.addValidator(name.textProperty(),
            { if (isIdentifier(it)) null else resources.getString(LC_ERROR_FORMAT) }) { decorateError(name, it) }
        validator.addValidator(name.textProperty(),
            { if (known.contains(it)) resources.getString(LC_ERROR_UNIQUE) else null }) { decorateError(name, it) }
        validator.addValidator(label.textProperty(), {
            if (isText(it)) null else resources.getString(LC_ERROR_FORMAT)
        }) { decorateError(label, it) }
        validator.validProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            dialogPane.buttonTypes.filtered { buttonType: ButtonType -> buttonType.buttonData.isDefaultButton }
                .forEach { dialogPane.lookupButton(it).isDisable = !newValue }
        }
        onShownProperty().addListener { _: ObservableValue<out EventHandler<DialogEvent>>, _: EventHandler<DialogEvent>, _: EventHandler<DialogEvent> -> onShow() }
        runLater { onShow() }
    }

    fun isEdit(): Boolean =
        edit.value

    fun setEdit(edit: Boolean) {
        this.edit.value = edit
    }

    private fun onShow() {
        validator.validate()
        name.requestFocus()
    }

    companion object {
        private const val CLASS_DANGER = "danger"
        private const val LC_CANCEL = "cancel"
        private const val LC_ERROR_EMPTY = "error.empty"
        private const val LC_ERROR_FORMAT = "error.format"
        private const val LC_ERROR_UNIQUE = "error.unique"
        private const val LC_OK_CREATE = "ok_create"
        private const val LC_OK_EDIT = "ok_edit"
        private const val LC_TITLE_CREATE = "title_create"
        private const val LC_TITLE_EDIT = "title_edit"
        private fun createOk(resources: ResourceBundle, newValue: Boolean): String =
            resources.getString(if (newValue) LC_OK_EDIT else LC_OK_CREATE)

        private fun createTitle(resources: ResourceBundle, value: Boolean): String =
            resources.getString(if (value) LC_TITLE_EDIT else LC_TITLE_CREATE)

        private fun decorateError(cell: Control?, errors: Collection<String>) {
            if (errors.isEmpty()) {
                classRemove(cell!!, CLASS_DANGER)
                cell.tooltip = null
            } else {
                classAdd(cell!!, CLASS_DANGER)
                cell.tooltip = Tooltip(errors.joinToString("\n"))
            }
        }
    }
}
