package gargoyle.rpycg.ui;

import gargoyle.fx.FXContext;
import gargoyle.fx.FXContextFactory;
import gargoyle.fx.FXLauncher;
import gargoyle.fx.FXUtil;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.model.Settings;
import gargoyle.rpycg.service.LocaleConverter;
import gargoyle.rpycg.ui.flags.Flags;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public final class TabSettings extends GridPane implements Initializable {
    private static final String LC_NEED_RESTART = "need-restart";
    private static final String LC_NEED_RESTART_CANCEL = "need-restart-cancel";
    private static final String LC_NEED_RESTART_OK = "need-restart-ok";
    private static final String PREF_GAME = "game";
    private static final String PREF_STORAGE = "storage";
    private static final String USER_HOME = System.getProperty("user.home");
    private final LocaleConverter localeConverter;
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
        final FXContext context = FXContextFactory.currentContext();
        preferences = context.getPreferences();
        localeConverter = new LocaleConverter(context);
        context.loadComponent(this).orElseThrow(() ->
                new AppUserException("No view {resource}", Map.of("resource", TabSettings.class.getName())));
    }

    public Path getGameDirectory() {
        return Paths.get(preferences.get(PREF_GAME, USER_HOME));
    }

    public void setGameDirectory(final Path gameDirectory) {
        preferences.put(PREF_GAME, gameDirectory.toFile().getAbsolutePath());
    }

    public Settings getSettings() {
        return settings;
    }

    public Path getStorageDirectory() {
        return Paths.get(preferences.get(PREF_STORAGE, USER_HOME));
    }

    public void setStorageDirectory(final Path storageDirectory) {
        preferences.put(PREF_STORAGE, storageDirectory.toFile().getAbsolutePath());
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        settings = initializeSettings(new Settings(localeConverter, preferences,
                chkEnableCheat.isSelected(), chkEnableConsole.isSelected(),
                chkEnableDeveloper.isSelected(), chkEnableWrite.isSelected(), chkEnableRollback.isSelected(),
                keyCheat.getDefaultCombination(), keyConsole.getDefaultCombination(),
                keyDeveloper.getDefaultCombination(), keyWrite.getDefaultCombination(),
                Locale.ENGLISH));
        initializeLocaleComboBox(cmbLocaleMenu, settings.getLocaleMenu());
        initializeLocaleUi(FXUtil.requireNonNull(resources, "No resources {location}",
                Map.of("location", location.toExternalForm())));
    }

    @FXML
    void onResetKeys(final ActionEvent actionEvent) {
        for (final KeyText keyText : new KeyText[]{keyCheat, keyConsole, keyWrite}) {
            keyText.reset();
        }
    }

    private void initializeLocaleComboBox(final ComboBox<Locale> comboBox, final Locale locale) {
        final Callback<ListView<Locale>, ListCell<Locale>> cellFactory = param ->
                new DecoratedListCell<>((cell, item) -> {
                    if (null == item) {
                        cell.setGraphic(null);
                        cell.setText("");
                    } else {
                        Flags.getFlag(FXContextFactory.currentContext(), item.getLanguage()).ifPresent(cell::setGraphic);
                        cell.setText(localeConverter.toDisplayString(item));
                    }
                });
        comboBox.setButtonCell(cellFactory.call(null));
        comboBox.setCellFactory(cellFactory);
        comboBox.getItems().setAll(localeConverter.getLocales());
        final Locale similarLocale = localeConverter.getSimilarLocale(locale);
        comboBox.setValue(similarLocale);
        comboBox.getSelectionModel().select(similarLocale);
    }

    private void initializeLocaleUi(final ResourceBundle resources) {
        final FXContext context = FXContextFactory.currentContext();
        cmbLocaleUi.getItems().setAll(localeConverter.getLocales().stream().map(locale ->
                Flags.getFlag(context, locale.getLanguage())
                        .map(imageView -> {
                            final MenuItem menuItem = new MenuItem(localeConverter.toDisplayString(locale), imageView);
                            menuItem.setOnAction(event -> {
                                FXContextFactory.changeContext(context.toBuilder()
                                        .setLocale(localeConverter.getSimilarLocale(locale)).createContext());
                                if (context.confirm(resources.getString(LC_NEED_RESTART), Map.of(
                                        ButtonBar.ButtonData.OK_DONE, resources.getString(LC_NEED_RESTART_OK),
                                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_NEED_RESTART_CANCEL)))) {
                                    FXLauncher.requestRestart(context.toBuilder().setLocale(locale).createContext());
                                }
                            });
                            return menuItem;
                        }).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        settings.localeMenuProperty().addListener((observable, oldValue, newValue) -> {
            cmbLocaleUi.setText(localeConverter.toDisplayString(newValue));
            Flags.getFlag(context, newValue.getLanguage()).ifPresent(cmbLocaleUi::setGraphic);
        });
        final Locale currentLocale = localeConverter.getSimilarLocale(context.getLocale());
        cmbLocaleUi.setText(localeConverter.toDisplayString(currentLocale));
        Flags.getFlag(context, currentLocale.getLanguage()).ifPresent(cmbLocaleUi::setGraphic);
    }

    private Settings initializeSettings(final Settings set) {
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

    private static final class DecoratedListCell<T> extends ListCell<T> {
        private final BiConsumer<? super ListCell<T>, ? super T> decorator;

        public DecoratedListCell(final BiConsumer<? super ListCell<T>, ? super T> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(final T item, final boolean empty) {
            super.updateItem(item, empty);
            decorator.accept(this, empty ? null : item);
        }
    }
}
