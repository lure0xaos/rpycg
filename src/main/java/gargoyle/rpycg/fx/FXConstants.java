package gargoyle.rpycg.fx;

public final class FXConstants {
    public static final String CMD_SKIN = "skin";
    public static final String EXT_CSS = "css";
    public static final String EXT_FXML = "fxml";
    public static final String EXT_GIF = "gif";
    public static final String EXT_JPG = "jpg";
    public static final String EXT_PNG = "png";
    public static final String EXT_PROPERTIES = "properties";
    public static final String EXT__IMAGES = String.join(",", EXT_PNG, EXT_GIF, EXT_JPG);
    public static final String KEY_SPLASH = "fx.splash";
    public static final String KEY_SPLASH_CLASS = "fx.splash-class";
    public static final String MSG_ERROR_LOADING_COMPONENT = "Error loading component {0}";
    public static final String MSG_ERROR_NO_RESOURCE = "Can''t find resources for {0}[{1}]";
    public static final String MSG_ERROR_NO_RESOURCES = "Can''t find resources for {0}";
    public static final String PREF_LOCALE = "locale";
    public static final String PREF_SKIN = "skin";
    public static final String SPLASH_CLASS_DEFAULT = FXImageSplash.class.getName();
    public static final String SPLASH_DEFAULT = "loading.gif";

    private FXConstants() {
        throw new IllegalStateException(FXConstants.class.getName());
    }
}
