package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXApplication;
import gargoyle.rpycg.fx.FXComponent;
import gargoyle.rpycg.fx.FXConstants;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.service.LocaleConverter;
import javafx.scene.Parent;

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
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, RPyCGApp.class.getName()));
        return component;
    }

    @Override
    public void doStop() {
        if (null != component) {
            component.getView().close();
        }
    }

}
