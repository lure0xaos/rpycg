package gargoyle.fx;

import gargoyle.fx.log.FXLog;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class FXUtil {

    private static final ResourceBundle.Control CONTROL =
            ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
    private static final Pattern PATTERN_FORMAT = Pattern.compile("\\{([^}{'\\s]+)}");

    private FXUtil() {
        throw new IllegalStateException(FXUtil.class.getName());
    }

    public static Optional<URL> findResource(final Class<?> baseClass, final Locale locale,
                                             final String baseName, final String suffixes) {
        final ClassLoader classLoader = baseClass.getClassLoader();
        final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, locale);
        for (final String suffix : suffixes.split(",")) {
            for (final Locale specificLocale : candidateLocales) {
                final String bundleName = CONTROL.toBundleName(baseName, specificLocale);
                final String resourceName = CONTROL.toResourceName(bundleName, suffix);
                final String absoluteResourceName = !resourceName.isEmpty() && '/' == resourceName.charAt(0)
                        ? resourceName : '/' + resourceName;
                for (final URL url : new URL[]{
                        getResource(baseClass, resourceName), getResource(classLoader, resourceName),
                        getResource(baseClass, absoluteResourceName), getResource(classLoader, absoluteResourceName)}) {
                    if (null != url) {
                        FXLog.debug("found ({baseClassName}){baseName}.{suffix} (from {resourceName})", Map.of(
                                "baseClassName", baseClass.getName(), "baseName", baseName,
                                "suffix", suffix, "resourceName", resourceName));
                        return Optional.of(url);
                    }
                }
            }
        }
        FXLog.warn("Can''t find resource for {baseName}[{suffixes}]", Map.of(
                "baseName", baseName, "suffixes", suffixes));
        FXLog.warn("Does {module} opens {basePackage}?", Map.of(
                "module", baseClass.getModule(),
                "basePackage", getPackage(baseName)));
        return Optional.empty();
    }

    private static String getPackage(final String baseName) {
        final String relative = !baseName.isEmpty() && baseName.charAt(0) == '/' ? baseName.substring(1) : baseName;
        final int endIndex = relative.lastIndexOf('/');
        final String substring = endIndex < 0 ? relative : relative.substring(0, endIndex);
        final String replace = substring.replace('/', '.');
        return substring;
    }

    public static Optional<Scene> findScene(final Node node) {
        return Optional.ofNullable(node).map(Node::getScene);
    }

    public static Optional<Scene> findScene(final Window node) {
        return Optional.ofNullable(node).map(Window::getScene);
    }

    public static Optional<Stage> findStage(final Node node) {
        return Optional.ofNullable(node).map(Node::getScene)
                .map(Scene::getWindow)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast);
    }

    public static List<String> format(final List<String> formats, final Map<String, ?> values) {
        return formats.stream().map(format -> format(format, values)).collect(Collectors.toList());
    }

    public static String format(final String format, final Map<String, ?> values) {
        return PATTERN_FORMAT.matcher(format).replaceAll(match -> {
            final Object value = values.get(match.group(1));
            return null != value ? value.toString() : match.group(0);
        });
    }

    public static Scene getOrCreateScene(final Parent parent) {
        return Optional.ofNullable(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
    }

    public static Optional<ResourceBundle> loadResources(final Class<?> baseClass, final Locale locale,
                                                         final String baseName) {
        try {
            return Optional.ofNullable(AccessController.doPrivileged((PrivilegedExceptionAction<ResourceBundle>) () ->
                    ResourceBundle.getBundle(baseName.charAt(0) == '/' ? baseName.substring(1) : baseName,
                            locale, baseClass.getClassLoader())));
        } catch (final MissingResourceException e) {
            FXLog.error("Can''t find resources for {baseName}",
                    Map.of("baseName", baseName));
            return Optional.empty();
        } catch (final PrivilegedActionException e) {
            FXLog.error(e, "Can''t load resources for {baseName}",
                    Map.of("baseName", baseName));
            return Optional.empty();
        }
    }

    public static String message(final Class<?> baseClass, final Locale locale, final String code,
                                 final Map<String, ?> args) {
        return message(baseClass, locale, () -> {
            FXLog.error("{code} not found in resources of {baseClass}", Map.of(
                    "code", code, "baseClass", baseClass));
            return FXUtil.format(code, args);
        }, code, args);
    }

    public static String message(final Class<?> baseClass, final Locale locale, final Supplier<String> defaultMessage,
                                 final String code, final Map<String, ?> args) {
        return loadResources(baseClass, locale, resolveBaseName(baseClass))
                .filter(resourceBundle -> resourceBundle.containsKey(code))
                .map(resourceBundle -> resourceBundle.getString(code))
                .map(s -> FXUtil.format(s, args))
                .orElseGet(defaultMessage);
    }

    public static <T> T requireNonNull(final T obj, final Supplier<String> messageSupplier) {
        if (null == obj) {
            throw new FXException(null == messageSupplier ? "" : messageSupplier.get());
        }
        return obj;
    }

    public static <T> T requireNonNull(final T obj, final String messageKey, final Map<String, ?> args) {
        if (null == obj) {
            throw new FXUserException(messageKey, args);
        }
        return obj;
    }

    public static String resolveBaseName(final FXComponent<?, ?> component, final String baseName) {
        return resolveBaseName(component.getBaseName(), baseName);
    }

    public static String resolveBaseName(final FXComponent<?, ?> component) {
        return component.getBaseName();
    }

    public static String resolveBaseName(final Class<?> aClass) {
        return resolveBaseName(aClass, aClass.getSimpleName());
    }

    public static String resolveBaseName(final Class<?> aClass, final String baseName) {
        return baseName.isEmpty() ? ('/' + aClass.getPackage().getName().replace('.', '/')) :
                (('/' == baseName.charAt(0)) ? baseName
                        : ('/' + aClass.getPackage().getName().replace('.', '/') + '/' + baseName));
    }

    public static String resolveBaseName(final String baseName, final String name) {
        return baseName.isEmpty() || name.isEmpty() ? "" : '/' == name.charAt(0) ? name :
                baseName.substring(0, baseName.lastIndexOf('/')) + '/' + name;
    }

    public static String stringStackTrace(final Throwable e) {
        try (final StringWriter stringWriter = new StringWriter();
             final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (final IOException exception) {
            return e.getLocalizedMessage();
        }
    }

    static <T> Collector<T, ?, T> toSingleton(final Supplier<? extends RuntimeException> exceptionSupplier) {
        return Collectors.collectingAndThen(Collectors.toList(), list -> {
            if (1 == list.size()) return list.get(0);
            throw exceptionSupplier.get();
        });
    }

    private static URL getResource(final ClassLoader classLoader, final String resourceName) {
        return AccessController.doPrivileged((PrivilegedAction<URL>) () -> classLoader.getResource(resourceName));
    }

    private static URL getResource(final Class<?> baseClass, final String resourceName) {
        return AccessController.doPrivileged((PrivilegedAction<URL>) () -> baseClass.getResource(resourceName));
    }
}
