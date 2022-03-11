package gargoyle.fx

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FxConstants {
    const val CMD_SKIN = "skin"
    const val EXT_CSS = "css"
    const val EXT_FXML = "fxml"
    const val EXT_GIF = "gif"
    const val EXT_JPG = "jpg"
    const val EXT_PNG = "png"
    const val EXT_PROPERTIES = "properties"
    const val EXT__IMAGES = "$EXT_PNG,$EXT_GIF,$EXT_JPG"
    const val KEY_SPLASH = "fx.splash"
    const val KEY_SPLASH_CLASS = "fx.splash-class"
    const val PREF_LOCALE = "locale"
    const val PREF_SKIN = "skin"
    val SPLASH_CLASS_DEFAULT: String = FxImageSplash::class.qualifiedName!!
    const val SPLASH_DEFAULT = "loading.gif"

}
