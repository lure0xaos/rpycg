package gargoyle.fx;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public final class FXFilePreferencesFactory implements PreferencesFactory {
    public FXFilePreferencesFactory() {
    }

    @Override
    public Preferences systemRoot() {
        return FXFilePreferences.getSystemRoot();
    }

    @Override
    public Preferences userRoot() {
        return FXFilePreferences.getUserRoot();
    }
}
