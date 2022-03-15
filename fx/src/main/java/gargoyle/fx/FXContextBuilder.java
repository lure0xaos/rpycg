package gargoyle.fx;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FXContextBuilder {
    private Class<?> baseClass;
    private Charset charset = StandardCharsets.UTF_8;
    private Locale locale;
    private FXRegistry registry = new FXRegistry();
    private String skin = "";

    public FXContextBuilder() {
    }

    public FXContext createContext() {
        return new FXContextImpl(registry, baseClass, charset, locale, skin);
    }

    public Class<?> getBaseClass() {
        return baseClass;
    }

    public FXContextBuilder setBaseClass(final Class<?> baseClass) {
        this.baseClass = baseClass;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public FXContextBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public FXContextBuilder setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public String getSkin() {
        return skin;
    }

    public FXContextBuilder setSkin(final String skin) {
        this.skin = skin;
        return this;
    }

    FXRegistry getRegistry() {
        return registry;
    }

    FXContextBuilder setRegistry(final FXRegistry registry) {
        this.registry = registry;
        return this;
    }
}
