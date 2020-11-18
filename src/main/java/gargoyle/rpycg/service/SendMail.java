package gargoyle.rpycg.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public final class SendMail {
    private static final Logger log = LoggerFactory.getLogger(SendMail.class);

    private SendMail() {
        throw new IllegalStateException(getClass().getName());
    }

    public static void mail(@NotNull String email, @NotNull String subject, @NotNull String body) {
        try {
            Desktop.getDesktop().mail(URI.create(MessageFormat.format("mailto:{0}?subject={1}&body={2}",
                    encode(email), encode(subject), encode(body))));
        } catch (UnsupportedOperationException | IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    @NotNull
    private static String encode(@NotNull String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
