package gargoyle.rpycg.model;

import gargoyle.rpycg.service.KeyConverter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCodeCombination;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

public final class Settings {
    private static final String PREF_CHEAT = "cheat";
    private static final String PREF_CONSOLE = "console";
    private static final String PREF_DEVELOPER = "developer";
    private static final String PREF_KEY_CHEAT = "keyCheat";
    private static final String PREF_KEY_CONSOLE = "keyConsole";
    private static final String PREF_KEY_DEVELOPER = "keyDeveloper";
    private static final String PREF_KEY_WRITE = "keyWrite";
    private static final String PREF_ROLLBACK = "rollback";
    private static final String PREF_WRITE = "write";
    private final KeyConverter converter;
    private final Property<Boolean> enableCheat;
    private final Property<Boolean> enableConsole;
    private final Property<Boolean> enableDeveloper;
    private final Property<Boolean> enableRollback;
    private final Property<Boolean> enableWrite;
    private final Property<KeyCodeCombination> keyCheat;
    private final Property<KeyCodeCombination> keyConsole;
    private final Property<KeyCodeCombination> keyDeveloper;
    private final Property<KeyCodeCombination> keyWrite;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public Settings(@NotNull Preferences preferences,
                    boolean enableCheat, boolean enableConsole, boolean enableDeveloper,
                    boolean enableWrite, boolean enableRollback,
                    @NotNull KeyCodeCombination keyCheat, @NotNull KeyCodeCombination keyConsole,
                    @NotNull KeyCodeCombination keyDeveloper, @NotNull KeyCodeCombination keyWrite) {
        this(enableCheat, enableConsole, enableDeveloper, enableWrite, enableRollback,
                keyCheat, keyConsole, keyDeveloper, keyWrite);
        this.enableCheat.setValue(preferences.getBoolean(PREF_CHEAT, enableCheat));
        this.enableConsole.setValue(preferences.getBoolean(PREF_CONSOLE, enableConsole));
        this.enableDeveloper.setValue(preferences.getBoolean(PREF_DEVELOPER, enableDeveloper));
        this.enableWrite.setValue(preferences.getBoolean(PREF_WRITE, enableWrite));
        this.keyCheat.setValue(converter.toCombination(preferences.get(PREF_KEY_CHEAT,
                converter.toString(keyCheat)), keyCheat));
        this.keyConsole.setValue(converter.toCombination(preferences.get(PREF_KEY_CONSOLE,
                converter.toString(keyConsole)), keyConsole));
        this.keyDeveloper.setValue(converter.toCombination(preferences.get(PREF_KEY_DEVELOPER,
                converter.toString(keyDeveloper)), keyDeveloper));
        this.keyWrite.setValue(converter.toCombination(preferences.get(PREF_KEY_WRITE,
                converter.toString(keyWrite)), keyWrite));

        this.enableCheat.addListener((observable, oldValue, newValue) ->
                preferences.putBoolean(PREF_CHEAT, newValue));
        this.enableConsole.addListener((observable, oldValue, newValue) ->
                preferences.putBoolean(PREF_CONSOLE, newValue));
        this.enableDeveloper.addListener((observable, oldValue, newValue) ->
                preferences.putBoolean(PREF_DEVELOPER, newValue));
        this.enableWrite.addListener((observable, oldValue, newValue) ->
                preferences.putBoolean(PREF_WRITE, newValue));
        this.enableRollback.addListener((observable, oldValue, newValue) ->
                preferences.putBoolean(PREF_ROLLBACK, newValue));
        this.keyCheat.addListener((observable, oldValue, newValue) ->
                preferences.put(PREF_KEY_CHEAT, converter.toString(keyCheat)));
        this.keyConsole.addListener((observable, oldValue, newValue) ->
                preferences.put(PREF_KEY_CHEAT, converter.toString(keyConsole)));
        this.keyDeveloper.addListener((observable, oldValue, newValue) ->
                preferences.put(PREF_KEY_CHEAT, converter.toString(keyDeveloper)));
        this.keyWrite.addListener((observable, oldValue, newValue) ->
                preferences.put(PREF_KEY_CHEAT, converter.toString(keyWrite)));
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public Settings(boolean enableCheat, boolean enableConsole, boolean enableDeveloper,
                    boolean enableWrite, boolean enableRollback,
                    @NotNull KeyCodeCombination keyCheat, @NotNull KeyCodeCombination keyConsole,
                    @NotNull KeyCodeCombination keyDeveloper, @NotNull KeyCodeCombination keyWrite) {
        converter = new KeyConverter();
        this.enableCheat = new SimpleBooleanProperty(enableCheat);
        this.enableConsole = new SimpleBooleanProperty(enableConsole);
        this.enableDeveloper = new SimpleBooleanProperty(enableDeveloper);
        this.enableWrite = new SimpleBooleanProperty(enableWrite);
        this.enableRollback = new SimpleBooleanProperty(enableRollback);
        this.keyCheat = new SimpleObjectProperty<>(keyCheat);
        this.keyConsole = new SimpleObjectProperty<>(keyConsole);
        this.keyDeveloper = new SimpleObjectProperty<>(keyDeveloper);
        this.keyWrite = new SimpleObjectProperty<>(keyWrite);
    }

    @NotNull
    public Property<Boolean> enableCheatProperty() {
        return enableCheat;
    }

    @NotNull
    public Property<Boolean> enableConsoleProperty() {
        return enableConsole;
    }

    @NotNull
    public Property<Boolean> enableDeveloperProperty() {
        return enableDeveloper;
    }

    @NotNull
    public Property<Boolean> enableRollbackProperty() {
        return enableRollback;
    }

    @NotNull
    public Property<Boolean> enableWriteProperty() {
        return enableWrite;
    }

    public Boolean getEnableCheat() {
        return enableCheat.getValue();
    }

    public void setEnableCheat(Boolean enableCheat) {
        this.enableCheat.setValue(enableCheat);
    }

    public Boolean getEnableConsole() {
        return enableConsole.getValue();
    }

    public void setEnableConsole(Boolean enableConsole) {
        this.enableConsole.setValue(enableConsole);
    }

    public Boolean getEnableDeveloper() {
        return enableDeveloper.getValue();
    }

    public void setEnableDeveloper(Boolean enableDeveloper) {
        this.enableDeveloper.setValue(enableDeveloper);
    }

    public Boolean getEnableRollback() {
        return enableRollback.getValue();
    }

    public void setEnableRollback(Boolean enableRollback) {
        this.enableRollback.setValue(enableRollback);
    }

    public Boolean getEnableWrite() {
        return enableWrite.getValue();
    }

    public void setEnableWrite(Boolean enableWrite) {
        this.enableWrite.setValue(enableWrite);
    }

    public KeyCodeCombination getKeyCheat() {
        return keyCheat.getValue();
    }

    public void setKeyCheat(KeyCodeCombination keyCheat) {
        this.keyCheat.setValue(keyCheat);
    }

    public KeyCodeCombination getKeyConsole() {
        return keyConsole.getValue();
    }

    public void setKeyConsole(KeyCodeCombination keyConsole) {
        this.keyConsole.setValue(keyConsole);
    }

    public KeyCodeCombination getKeyDeveloper() {
        return keyDeveloper.getValue();
    }

    public void setKeyDeveloper(KeyCodeCombination keyDeveloper) {
        this.keyDeveloper.setValue(keyDeveloper);
    }

    public KeyCodeCombination getKeyWrite() {
        return keyWrite.getValue();
    }

    public void setKeyWrite(KeyCodeCombination keyWrite) {
        this.keyWrite.setValue(keyWrite);
    }

    @NotNull
    public Property<KeyCodeCombination> keyCheatProperty() {
        return keyCheat;
    }

    @NotNull
    public Property<KeyCodeCombination> keyConsoleProperty() {
        return keyConsole;
    }

    @NotNull
    public Property<KeyCodeCombination> keyDeveloperProperty() {
        return keyDeveloper;
    }

    @NotNull
    public Property<KeyCodeCombination> keyWriteProperty() {
        return keyWrite;
    }
}
