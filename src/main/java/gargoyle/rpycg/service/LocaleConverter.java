package gargoyle.rpycg.service;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public final class LocaleConverter {

    @NotNull
    private final ResourceBundle resources;

    public LocaleConverter() {
        this(FXContextFactory.currentContext());
    }

    public LocaleConverter(FXContext context) {
        resources = FXLoad.loadResources(context, FXLoad.getBaseName(getClass()))
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_RESOURCES, getClass().getName()));
    }

    @NotNull
    public Set<Locale> getLocales() {
        return Arrays.stream(resources.getString("supported_locales").split(","))
                .map(this::toLocale).collect(Collectors.toSet());
    }

    @NotNull
    public Locale toLocale(@NotNull String loc) {
        String[] split = loc.split("_");
        int length = split.length;
        switch (length) {
            case 0:
                return new Locale(loc);
            case 1:
                return new Locale(split[0]);
            case 2:
                return new Locale(split[0], split[1]);
            case 3:
                return new Locale(split[0], split[1], split[2]);
            default:
                return new Locale(loc);
        }
    }

    @NotNull
    public Locale getSimilarLocale(@NotNull Collection<Locale> locales, @NotNull Locale locale) {
        return locales.stream().filter(loc -> Objects.equals(locale.getLanguage(), loc.getLanguage())).findAny()
                .orElseGet(() -> locales.stream().findAny().orElse(Locale.getDefault()));
    }

    @NotNull
    public String toDisplayString(Locale locale) {
        return MessageFormat.format("{0} ({1})",
                locale.getDisplayLanguage(FXContextFactory.currentContext().getLocale()),
                locale.getDisplayLanguage(locale));
    }

    @NotNull
    public String toString(@NotNull Locale locale) {
        boolean haLanguage = !locale.getLanguage().isEmpty();
        boolean hasScript = !locale.getScript().isEmpty();
        boolean hasCountry = !locale.getCountry().isEmpty();
        boolean hasVariant = !locale.getVariant().isEmpty();
        boolean hasExtension = false;
        StringBuilder result = new StringBuilder(locale.getLanguage());
        if (hasCountry || (haLanguage && (hasVariant || hasScript || hasExtension))) {
            result.append('_').append(locale.getCountry());
        }
        if (hasVariant && (haLanguage || hasCountry)) {
            result.append('_').append(locale.getVariant());
        }
        if (hasScript && (haLanguage || hasCountry)) {
            result.append("_#").append(locale.getScript());
        }
        return result.toString();
    }
}
