package gargoyle.fx

@Suppress("MemberVisibilityCanBePrivate")
object FxConstants {
    const val CMD_SKIN: String = "skin"
    const val EXT_CSS: String = "css"
    const val EXT_FXML: String = "fxml"
    const val EXT_GIF: String = "gif"
    const val EXT_JPG: String = "jpg"
    const val EXT_PNG: String = "png"
    const val EXT_PROPERTIES: String = "properties"
    const val EXT__IMAGES: String = "$EXT_PNG,$EXT_GIF,$EXT_JPG"
    const val KEY_SPLASH: String = "fx.splash"
    const val KEY_SPLASH_CLASS: String = "fx.splash-class"
    const val PREF_LOCALE: String = "locale"
    const val PREF_SKIN: String = "skin"
    val SPLASH_CLASS_DEFAULT: String = FxImageSplash::class.qualifiedName!!
    const val SPLASH_DEFAULT: String = "loading.gif"
}
