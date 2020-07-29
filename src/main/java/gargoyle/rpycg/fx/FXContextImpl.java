package gargoyle.rpycg.fx;

import javafx.application.Application;
import javafx.application.HostServices;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class FXContextImpl implements FXContext {
    private Charset charset;
    private ClassLoader classLoader;
    private HostServices hostServices;
    private Locale locale;
    private Application.Parameters parameters;

    FXContextImpl() {
        charset = StandardCharsets.UTF_8;
        locale = Locale.getDefault();
    }

    @Override
    @NotNull
    public Charset getCharset() {
        return charset;
    }

    @Override
    @NotNull
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    @NotNull
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Application.Parameters getParameters() {
        return parameters;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setParameters(Application.Parameters parameters) {
        this.parameters = parameters;
    }

    public void setLocale(@NotNull Locale locale) {
        this.locale = locale;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void setCharset(@NotNull Charset charset) {
        this.charset = charset;
    }
}
