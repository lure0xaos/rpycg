package gargoyle.rpycg.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

@SuppressWarnings("HardCodedStringLiteral")
public final class RegisterLaunch4j {
    public static final int MILLIS = 10000;

    private RegisterLaunch4j() {
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static void main(String[] args) {
        String version = System.getProperty("java.version");
        String javaHome = System.getProperty("java.home");
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("register-launch4j", ".reg");
            String content = MessageFormat.format("Windows Registry Editor Version 5.00\n\n[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft]\n\n[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit]\n\"CurrentVersion\"=\"{0}\"\n\n[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\{1}]\n\"JavaHome\"=\"{2}\"\n\"MicroVersion\"=\"0\"",
                    version, version,
                    javaHome.replace("\\", "\\\\"));
            Files.writeString(tempFile, content);
            Desktop.getDesktop().open(tempFile.toFile());
            Thread.sleep(MILLIS);
        } catch (IOException e) {
            throw new IllegalStateException("Error reg", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.delete(tempFile);
                } catch (IOException e) {
                    throw new IllegalStateException("not deleted", e);
                }
            }
        }
    }
}
