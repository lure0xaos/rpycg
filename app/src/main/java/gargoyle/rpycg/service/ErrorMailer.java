package gargoyle.rpycg.service;

import gargoyle.fx.FXHolder;
import gargoyle.fx.FXUtil;

import java.util.Map;
import java.util.ResourceBundle;

public class ErrorMailer {
    private static final String KEY_BODY = "mail.body";
    private static final String KEY_EMAIL = "mail.email";
    private static final String KEY_SUBJECT = "mail.subject";
    private static final FXHolder<ResourceBundle> resourceBundleHolder = new FXHolder<>(() ->
            ResourceBundle.getBundle(ErrorMailer.class.getName().replace('.', '/')));

    public static void mailError(final Exception e) {
        final ResourceBundle resources = resourceBundleHolder.get();
        SendMail.mail(resources.getString(KEY_EMAIL), resources.getString(KEY_SUBJECT),
                FXUtil.format(resources.getString(KEY_BODY), Map.of("stacktrace", FXUtil.stringStackTrace(e))));
    }
}
