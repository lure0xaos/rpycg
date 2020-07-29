package gargoyle.rpycg.fx;

public final class FXConstants {
    public static final String EXT_CSS = "css";
    public static final String EXT_FXML = "fxml";
    public static final String EXT_GIF = "gif";
    public static final String EXT_JPG = "jpg";
    public static final String EXT_PNG = "png";
    public static final String EXT_IMAGES = String.join(",", EXT_PNG, EXT_GIF, EXT_JPG);
    public static final String MSG_ERROR_LOADING_COMPONENT = "Error loading component {}";
    public static final String MSG_ERROR_NO_RESOURCE = "Can''t find resources for {}[{}]";
    public static final String MSG_ERROR_NO_RESOURCES = "Can''t find resources for {}";

    private FXConstants() {
        throw new IllegalStateException(FXConstants.class.getName());
    }
}
