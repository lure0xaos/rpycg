package gargoyle.rpycg.service;

import gargoyle.fx.log.FXLog;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public final class SendMail {
    private static final String MAILTO_LINK = "mailto:{0}?subject={1}&body={2}";

    private SendMail() {
        throw new IllegalStateException(SendMail.class.getName());
    }

    public static void mail(final String email, final String subject, final String body) {
        try {
            Desktop.getDesktop().mail(URI.create(MessageFormat.format(MAILTO_LINK,
                    encode(email), encode(subject), encode(body))));
        } catch (final UnsupportedOperationException | IOException e) {
            FXLog.error(e, "mail");
        }
    }

    private static String encode(final String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
