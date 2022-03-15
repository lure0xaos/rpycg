package gargoyle.rpycg.ui.flags;

import gargoyle.fx.FXConstants;
import gargoyle.fx.FXContext;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Optional;

public final class Flags {
    private Flags() {
        throw new IllegalStateException(Flags.class.getName());
    }

    public static Optional<ImageView> getFlag(final FXContext context, final String flagBaseName) {
        return context.findResource(Flags.class, flagBaseName, FXConstants.EXT__IMAGES)
                .map(URL::toExternalForm).map(ImageView::new);
    }
}
