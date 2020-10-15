package gargoyle.rpycg.fx;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public final class FilePreferencesFactory implements PreferencesFactory {
    @Override
    public Preferences systemRoot() {
        return FilePreferences.getSystemRoot();
    }

    @Override
    public Preferences userRoot() {
        return FilePreferences.getUserRoot();
    }
}
