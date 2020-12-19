package gargoyle.rpycg.service;

import gargoyle.rpycg.fx.Logger;
import gargoyle.rpycg.fx.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public final class SendMail {
    private static final String MAILTO_LINK = "mailto:{0}?subject={1}&body={2}";
    private static final Logger log = LoggerFactory.getLogger(SendMail.class);

    private SendMail() {
        throw new IllegalStateException(SendMail.class.getName());
    }

    public static void mail(String email, String subject, String body) {
        try {
            Desktop.getDesktop().mail(URI.create(MessageFormat.format(MAILTO_LINK,
                    encode(email), encode(subject), encode(body))));
        } catch (UnsupportedOperationException | IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
