package gargoyle.fx;

import javafx.collections.ObservableMap;
import javafx.util.Callback;

enum FXCloseFlag {
    RESTART,
    PREVENT;

    @SuppressWarnings("unchecked")
    FXCloseAction doIf(final FXContext context) {
        final ObservableMap<Object, Object> properties = context.getStage().getProperties();
        if (properties.containsKey(this)) {
            final Callback<FXContext, FXCloseAction> callback = (Callback<FXContext, FXCloseAction>)
                    properties.get(this);
            properties.remove(this);
            return callback.call(context);
        }
        return FXCloseAction.CLOSE;
    }

    void set(final FXContext context, final Callback<FXContext, FXCloseAction> callback) {
        context.getStage().getProperties().put(this, callback);
    }
}
