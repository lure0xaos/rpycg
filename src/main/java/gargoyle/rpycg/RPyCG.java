package gargoyle.rpycg;

import gargoyle.rpycg.fx.FXLauncher;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.service.SendMail;
import gargoyle.rpycg.ui.RPyCGApp;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class RPyCG {
    private RPyCG() {
    }

    public static void mailError(@NotNull Exception e) {
        ResourceBundle resources = ResourceBundle.getBundle(RPyCG.class.getName().replace('.', '/'));
        SendMail.mail(resources.getString("mail.email"), resources.getString("mail.subject"),
                MessageFormat.format(resources.getString("mail.body"), FXUtil.stringStackTrace(e)));
    }

    public static void main(String[] args) {
        FXLauncher.run(RPyCGApp.class, args);
    }
}
