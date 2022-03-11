package gargoyle.rpycg.ui

import gargoyle.fx.FxApplication
import gargoyle.fx.FxComponent
import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import gargoyle.rpycg.ex.AppUserException
import gargoyle.rpycg.service.LocaleConverter
import javafx.scene.Parent

class RPyCGApp : FxApplication {
    private lateinit var component: FxComponent<Main, Main>
    override fun doInit() {
        val localeConverter = LocaleConverter(FxContext.current)
        FxContext.current = FxContext.current.toBuilder()
            .setLocale(
                localeConverter.toLocale(
                    FxContext.current.preferences[FxConstants.PREF_LOCALE, localeConverter.toString(
                        localeConverter.getSimilarLocale(
                            FxContext.current.locale
                        )
                    )]
                )
            )
            .setSkin(FxContext.current.preferences[FxConstants.PREF_SKIN, FxContext.current.skin])
            .build()
    }

    override fun doStart(): FxComponent<*, out Parent> {
        component = FxContext.current.loadComponent(Main::class) ?: throw AppUserException("No view {RPyCGApp}")
        return component
    }

    override fun doStop() {
        if (::component.isInitialized) component.view.close()
    }
}
