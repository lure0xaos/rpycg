package gargoyle.rpycg;

import gargoyle.rpycg.fx.FXHolder;
import gargoyle.rpycg.fx.FXLauncher;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.service.SendMail;
import gargoyle.rpycg.ui.RPyCGApp;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class RPyCG {

    private static final String KEY_BODY = "mail.body";
    private static final String KEY_EMAIL = "mail.email";
    private static final String KEY_SUBJECT = "mail.subject";
    private static final FXHolder<ResourceBundle> resourceBundleHolder = new FXHolder<>(() ->
            ResourceBundle.getBundle(RPyCG.class.getName().replace('.', '/')));

    private RPyCG() {
    }

    public static void mailError(@NotNull Exception e) {
        ResourceBundle resources = resourceBundleHolder.get();
        SendMail.mail(resources.getString(KEY_EMAIL), resources.getString(KEY_SUBJECT),
                MessageFormat.format(resources.getString(KEY_BODY), FXUtil.stringStackTrace(e)));
    }

    public static void main(String[] args) {
        FXLauncher.run(RPyCGApp.class, args);
    }
}
