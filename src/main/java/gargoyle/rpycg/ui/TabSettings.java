package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLauncher;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.model.Settings;
import gargoyle.rpycg.service.LocaleConverter;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public final class TabSettings extends GridPane implements Initializable {

    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.TabSettings")
    private static final String LC_NEED_RESTART = "need-restart";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.TabSettings")
    private static final String LC_NEED_RESTART_CANCEL = "need-restart-cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.TabSettings")
    private static final String LC_NEED_RESTART_OK = "need-restart-ok";
    private static final String PREF_GAME = "game";

    private final LocaleConverter localeConverter = new LocaleConverter();

    @NotNull
    private final Preferences preferences;
    @FXML
    private CheckBox chkEnableCheat;
    @FXML
    private CheckBox chkEnableConsole;
    @FXML
    private CheckBox chkEnableDeveloper;
    @FXML
    private CheckBox chkEnableRollback;
    @FXML
    private CheckBox chkEnableWrite;
    @FXML
    private ComboBox<Locale> cmbLocaleMenu;
    @FXML
    private MenuButton cmbLocaleUi;
    @FXML
    private KeyText keyCheat;
    @FXML
    private KeyText keyConsole;
    @FXML
    private KeyText keyDeveloper;
    @FXML
    private KeyText keyWrite;
    private Settings settings;

    public TabSettings() {
        preferences = Objects.requireNonNull(FXContextFactory.currentContext().getPreferences());
        FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, TabSettings.class.getName()));
    }

    @SuppressWarnings("AccessOfSystemProperties")
    @NotNull
    public Path getGameDirectory() {
        return Paths.get(preferences.get(PREF_GAME, System.getProperty("user.home")));
    }

    public void setGameDirectory(@NotNull Path gameDirectory) {
        preferences.put(PREF_GAME, gameDirectory.toFile().getAbsolutePath());
    }

    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        settings = initializeSettings(new Settings(preferences,
                chkEnableCheat.isSelected(), chkEnableConsole.isSelected(),
                chkEnableDeveloper.isSelected(), chkEnableWrite.isSelected(), chkEnableRollback.isSelected(),
                keyCheat.getDefaultCombination(), keyConsole.getDefaultCombination(),
                keyDeveloper.getDefaultCombination(), keyWrite.getDefaultCombination(),
                Locale.ENGLISH));
        initializeLocaleComboBox(cmbLocaleMenu, settings.getLocaleMenu());
        initializeLocaleUi(FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES,
                location.toExternalForm()));
    }

    @NotNull
    private Settings initializeSettings(@NotNull Settings set) {
        cmbLocaleMenu.getSelectionModel().select(set.getLocaleMenu());
        chkEnableRollback.selectedProperty().setValue(set.enableRollbackProperty().getValue());
        chkEnableCheat.selectedProperty().setValue(set.enableCheatProperty().getValue());
        chkEnableConsole.selectedProperty().setValue(set.enableConsoleProperty().getValue());
        chkEnableDeveloper.selectedProperty().setValue(set.enableDeveloperProperty().getValue());
        chkEnableWrite.selectedProperty().setValue(set.enableWriteProperty().getValue());
        keyCheat.defaultCombinationProperty().setValue(set.keyCheatProperty().getValue());
        keyConsole.defaultCombinationProperty().setValue(set.keyConsoleProperty().getValue());
        keyDeveloper.defaultCombinationProperty().setValue(set.keyDeveloperProperty().getValue());
        keyWrite.defaultCombinationProperty().setValue(set.keyWriteProperty().getValue());

        set.localeMenuProperty().bind(cmbLocaleMenu.getSelectionModel().selectedItemProperty());
        set.enableRollbackProperty().bind(chkEnableRollback.selectedProperty());
        set.enableCheatProperty().bind(chkEnableCheat.selectedProperty());
        set.enableConsoleProperty().bind(chkEnableConsole.selectedProperty());
        set.enableDeveloperProperty().bind(chkEnableDeveloper.selectedProperty());
        set.enableWriteProperty().bind(chkEnableWrite.selectedProperty());
        set.keyCheatProperty().bind(keyCheat.combinationProperty());
        set.keyConsoleProperty().bind(keyConsole.combinationProperty());
        set.keyDeveloperProperty().bind(keyDeveloper.combinationProperty());
        set.keyWriteProperty().bind(keyWrite.combinationProperty());

        keyCheat.disableProperty().bind(Bindings.not(chkEnableCheat.selectedProperty()));
        keyConsole.disableProperty().bind(Bindings.not(chkEnableConsole.selectedProperty()));
        keyDeveloper.disableProperty().bind(Bindings.not(chkEnableDeveloper.selectedProperty()));
        keyWrite.disableProperty().bind(Bindings.not(chkEnableWrite.selectedProperty()));
        return set;
    }

    private void initializeLocaleComboBox(@NotNull ComboBox<Locale> comboBox, @NotNull Locale locale) {
        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = param -> new DecoratedListCell<>((cell, item) -> {
            if (item == null) {
                cell.setGraphic(null);
                cell.setText("");
            } else {
                FXContext context = FXContextFactory.currentContext();
                context.findResource(context.getBaseName(TabSettings.class,
                        "flags/" + item.getLanguage()), FXConstants.EXT_IMAGES)
                        .map(URL::toExternalForm).map(ImageView::new).ifPresent(cell::setGraphic);
                cell.setText(localeConverter.toDisplayString(item));
            }
        });
        comboBox.setButtonCell(cellFactory.call(null));
        comboBox.setCellFactory(cellFactory);
        comboBox.getItems().setAll(localeConverter.getLocales());
        Locale similarLocale = localeConverter.getSimilarLocale(localeConverter.getLocales(), locale);
        comboBox.setValue(similarLocale);
        comboBox.getSelectionModel().select(similarLocale);
    }

    private void initializeLocaleUi(@NotNull ResourceBundle resources) {
        cmbLocaleUi.getItems().setAll(localeConverter.getLocales().stream().filter(Objects::nonNull).map(locale -> {
            MenuItem menuItem = new MenuItem(localeConverter.toDisplayString(locale), getFlag(locale));
            menuItem.setOnAction(event -> {
                FXContextFactory.changeLocale(locale);
                if (FXDialogs.confirm(getStage().orElse(null), resources.getString(LC_NEED_RESTART), Map.of(
                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_NEED_RESTART_OK),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_NEED_RESTART_CANCEL)))) {
                    FXLauncher.requestRestart(getStage().orElseThrow());
                }
            });
            return menuItem;
        }).collect(Collectors.toList()));
        settings.localeMenuProperty().addListener((observable, oldValue, newValue) -> {
            cmbLocaleUi.setText(localeConverter.toDisplayString(newValue));
            cmbLocaleUi.setGraphic(getFlag(newValue));
        });
        Locale currentLocale = localeConverter.getSimilarLocale(localeConverter.getLocales(),
                FXContextFactory.currentContext().getLocale());
        cmbLocaleUi.setText(localeConverter.toDisplayString(currentLocale));
        cmbLocaleUi.setGraphic(getFlag(currentLocale));
    }

    private Node getFlag(Locale locale) {
        FXContext context = FXContextFactory.currentContext();
        return context.findResource(context.getBaseName(TabSettings.class,
                MessageFormat.format("flags/{0}", locale.getLanguage())), FXConstants.EXT_IMAGES)
                .map(URL::toExternalForm).map(ImageView::new).orElse(null);
    }

    @NotNull
    private Optional<Stage> getStage() {
        return FXUtil.findStage(cmbLocaleMenu);
    }

    @FXML
    void onResetKeys(ActionEvent actionEvent) {
        for (KeyText keyText : new KeyText[]{keyCheat, keyConsole, keyWrite}) {
            keyText.reset();
        }
    }

    private static final class DecoratedListCell<T> extends ListCell<T> {
        private final BiConsumer<ListCell<T>, T> decorator;

        public DecoratedListCell(BiConsumer<ListCell<T>, T> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            decorator.accept(this, empty ? null : item);
        }
    }
}
