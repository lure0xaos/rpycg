package gargoyle.fx

import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

class FxFilePreferencesFactory : PreferencesFactory {
    override fun systemRoot(): Preferences =
        FxFilePreferences.systemRoot.value

    override fun userRoot(): Preferences =
        FxFilePreferences.userRoot.value
}
