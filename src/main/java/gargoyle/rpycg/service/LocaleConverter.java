package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.fx.log.FXLog;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class LocaleConverter {
    private static final String KEY_SUPPORTED_LOCALES = "supported_locales";
    private final ResourceBundle resources;

    public LocaleConverter(final FXContext context) {
        this(context.getBaseClass(), FXContextFactory.currentContext().getLocale());
    }

    LocaleConverter(final Class<?> baseClass, final Locale locale) {
        resources = Optional.of((LocaleConverter.class))
                .map(FXUtil::resolveBaseName)
                .map(baseName -> {
                    try {
                        return (ResourceBundle.getBundle(baseName, locale, baseClass.getClassLoader()));
                    } catch (final MissingResourceException e) {
                        FXLog.error(FXConstants.MSG_ERROR_NO_RESOURCES, baseName);
                        return null;
                    }
                }).orElseThrow(() ->
                        new AppUserException(AppUserException.LC_ERROR_NO_RESOURCES, LocaleConverter.class.getName()));
    }

    public Set<Locale> getLocales() {
        return Arrays.stream(resources.getString(KEY_SUPPORTED_LOCALES).split(","))
                .map(this::toLocale).collect(Collectors.toSet());
    }

    public Locale getSimilarLocale(final Locale locale) {
        return getSimilarLocale(getLocales(), locale);
    }

    public Locale getSimilarLocale(final Collection<Locale> locales, final Locale locale) {
        return locales.stream().filter(loc -> Objects.equals(locale.getLanguage(), loc.getLanguage())).findAny()
                .orElseGet(() -> locales.stream().findAny().orElse(Locale.getDefault()));
    }

    public String toDisplayString(final Locale locale) {
        return MessageFormat.format("{0} ({1})",
                locale.getDisplayLanguage(FXContextFactory.currentContext().getLocale()),
                locale.getDisplayLanguage(locale));
    }

    public Locale toLocale(final String loc) {
        final String[] parts = loc.trim().split("_");
        switch (parts.length) {
            case 3:
                return new Locale(parts[0], parts[1], parts[2]);
            case 2:
                return new Locale(parts[0], parts[1]);
            case 1:
                return new Locale(parts[0]);
            case 0:
            default:
                return new Locale(loc);
        }
    }

    public String toString(final Locale locale) {
        final boolean hasLanguage = !locale.getLanguage().isEmpty();
        final boolean hasScript = !locale.getScript().isEmpty();
        final boolean hasCountry = !locale.getCountry().isEmpty();
        final boolean hasVariant = !locale.getVariant().isEmpty();
        final StringBuilder result = new StringBuilder(locale.getLanguage());
        if (hasCountry || hasLanguage && (hasVariant || hasScript)) {
            result.append('_').append(locale.getCountry());
        }
        if (hasVariant && (hasLanguage || hasCountry)) {
            result.append('_').append(locale.getVariant());
        }
        if (hasScript && (hasLanguage || hasCountry)) {
            result.append("_#").append(locale.getScript());
        }
        return result.toString();
    }
}
