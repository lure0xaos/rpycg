package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.application.HostServices;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Locale;

public interface FXContext {

    @NotNull
    Charset getCharset();

    @NotNull
    ClassLoader getClassLoader();

    @Nullable
    HostServices getHostServices();

    @NotNull
    Locale getLocale();

    @Nullable
    Application.Parameters getParameters();
}
