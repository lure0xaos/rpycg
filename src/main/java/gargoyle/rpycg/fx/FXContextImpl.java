package gargoyle.rpycg.fx;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.prefs.Preferences;

final class FXContextImpl extends FXContext {
    private final Class<?> baseClass;
    private final Charset charset;
    private final Locale locale;
    private final Preferences preferences;
    private final FXRegistry registry;
    private final String skin;

    FXContextImpl(final FXRegistry registry, final Class<?> baseClass, final Charset charset, final Locale locale,
                  final String skin) {
        this.baseClass = baseClass;
        this.charset = charset;
        preferences = Preferences.userNodeForPackage(baseClass);
        this.locale = locale;
        this.registry = registry;
        this.skin = skin;
    }

    public Class<?> getBaseClass() {
        return baseClass;
    }

    public Charset getCharset() {
        return charset;
    }

    public Locale getLocale() {
        return locale;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String getSkin() {
        return skin;
    }

    @Override
    FXRegistry getRegistry() {
        return registry;
    }

    @Override
    public String toString() {
        return MessageFormat.format("FXContextImpl'{'baseClass={0}, charset={1}, locale={2}, skin=''{3}'''}'",
                baseClass, charset, locale, skin);
    }
}
