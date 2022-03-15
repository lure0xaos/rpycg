package gargoyle.rpycg.ui

import gargoyle.fx.FxApplication
import gargoyle.fx.FxComponent
import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import gargoyle.rpycg.service.LocaleConverter
import javafx.scene.Parent

class RPyCGApp : FxApplication {
    private lateinit var component: FxComponent<Main, Main>
    override fun doInit() {
        with(FxContext.current) {
            LocaleConverter(this)
                .let { localeConverter ->
                    FxContext.current = toBuilder()
                        .setLocale(
                            localeConverter.toLocale(
                                preferences[FxConstants.PREF_LOCALE,
                                        localeConverter.toString(localeConverter.getSimilarLocale(locale))]
                            )
                        ).also { builder ->
                            preferences[FxConstants.PREF_SKIN, skin]?.also { builder.setSkin(it) }
                        }.build()
                }
        }
    }

    override fun doStart(): FxComponent<*, out Parent> {
        component = FxContext.current.loadComponent(Main::class) ?: error("No view {RPyCGApp}")
        return component
    }

    override fun doStop() {
        if (::component.isInitialized) component.view.close()
    }
}
