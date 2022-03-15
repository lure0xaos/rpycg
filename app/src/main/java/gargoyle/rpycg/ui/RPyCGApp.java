package gargoyle.rpycg.ui;

import gargoyle.fx.FXApplication;
import gargoyle.fx.FXComponent;
import gargoyle.fx.FXConstants;
import gargoyle.fx.FXContext;
import gargoyle.fx.FXContextFactory;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.service.LocaleConverter;
import javafx.scene.Parent;

import java.util.Map;
import java.util.prefs.Preferences;

public final class RPyCGApp implements FXApplication {
    private FXComponent<Main, Main> component;

    @Override
    public void doInit() {
        final FXContext context = FXContextFactory.currentContext();
        final LocaleConverter localeConverter = new LocaleConverter(context);
        final Preferences preferences = context.getPreferences();
        FXContextFactory.changeContext(context.toBuilder()
                .setLocale(localeConverter.toLocale(preferences.get(FXConstants.PREF_LOCALE,
                        localeConverter.toString(localeConverter.getSimilarLocale(context.getLocale())))))
                .setSkin(preferences.get(FXConstants.PREF_SKIN, context.getSkin())).createContext()
        );
    }

    public FXComponent<?, ? extends Parent> doStart() {
        component = FXContextFactory.currentContext().loadComponent(Main.class)
                .orElseThrow(() -> new AppUserException("No view {resource}",
                        Map.of("resource", RPyCGApp.class.getName())));
        return component;
    }

    @Override
    public void doStop() {
        if (null != component) {
            component.getView().close();
        }
    }

}
