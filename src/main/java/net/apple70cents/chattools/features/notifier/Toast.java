package net.apple70cents.chattools.features.notifier;

import com.sshtools.twoslices.ToastType;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class Toast {
    private static String text;
    private static final CircuitBreakerExecutor toastExecutor = CircuitBreakerExecutor.of(() -> {
        final String TITLE = TextUtils.trans("texts.toast.title").getString();
        String truncatedText = truncateWithEllipsis(text, 250);
        switch (ConfigUtils.getString("notifier.Toast.Mode")) {
            case "AWT":
                toastWithAWT(TITLE, truncatedText);
                break;
            case "POWERSHELL":
//? if !LITE_VERSION {
                toastWithPowershell(TITLE, truncatedText);
//?}
                break;
            case "ADDON":
//? if !LITE_VERSION {
                toastWithAddon(TITLE, truncatedText);
//?}
                break;
            case "TWO_SLICES":
                toastWithTwoSlices(TITLE, truncatedText);
                break;
            default:
                return;
        }
    }).setMaxLimitPerSecond(() -> ConfigUtils.getInt("general.CircuitBreaker.ToastThreshold"))
    .setFailsafeFunction(() -> {
        ConfigUtils.set("notifier.Toast.Enabled", false);
        int threshold = ConfigUtils.getInt("general.CircuitBreaker.ToastThreshold");
        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold));
        MessageUtils.sendToActionbar(TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold));
        LoggerUtils.warn("[ChatTools] " + TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold).getString());
    }).setFailsafeJudgement(() -> ConfigUtils.getBoolean("notifier.Toast.Enabled"));

    public static void work(String text1) {
        text = text1;
        toastExecutor.run();
    }

//? if !LITE_VERSION {
    public static void toastWithAddon(String caption, String text) {
        if (!DownloadUtils.checkIfFullyReady()) {
            MessageUtils.sendToActionbar(TextUtils.trans("texts.toast.failure"));
            return;
        }
        LoggerUtils.info("[ChatTools] Trying to toast with addon.");
        Runnable toastRunnable = () -> {
            Map<String, String> map = DownloadUtils.getFileNamesMap();
            if (map == null || map.isEmpty()) {
                return;
            }
            String icon = map.get("icon");
            String toastExe = map.get("toastExe");
            File iconFile = Path.of(DownloadUtils.STORAGE_DIR.toString(), icon).toFile();
            File toastExeFile = Path.of(DownloadUtils.STORAGE_DIR.toString(), toastExe).toFile();
            String command = String.format("%s %s %s %s", '"' + toastExeFile.toString() + '"', '"' + caption + '"', '"' + text.replace("\n", "\\n") + '"', '"' + iconFile.toString() + '"');
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start()
                                                                                         .getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LoggerUtils.info("[ChatTools] " + line);
                }
            } catch (Exception e) {
                MessageUtils.sendToActionbar(TextUtils.trans("texts.toast.failure"));
                LoggerUtils.error("[ChatTools] Failed to send toast notification", e);
            }
        };
        TaskScheduler.schedule(toastRunnable, 0);
    }
//?}

    public static void toastWithAWT(String caption, String text) {
        LoggerUtils.info("[ChatTools] Toast Notified with AWT.");
        System.setProperty("java.awt.headless", "false");
        LoggerUtils.warn(String.format("[ChatTools] Set java.awt.headless to %s.", GraphicsEnvironment.isHeadless()));
        SwingUtilities.invokeLater(() -> {
            if (GraphicsEnvironment.isHeadless()) {
                System.setProperty("java.awt.headless", "false");
                LoggerUtils.warn(String.format("[ChatTools] GraphicsEnvironment.isHeadless() was true, but now it is set to %s.", GraphicsEnvironment.isHeadless()));
            }
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "ChatTools");
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                LoggerUtils.error("[ChatTools] Failed to add system tray icon", e);
            }
            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.NONE);
            tray.remove(trayIcon);
        });
    }

    /**
     * Escapes text safely for embedding inside PowerShell single-quoted string expressions.
     * In PowerShell single-quoted literals, single quotes are escaped via doubling (' -> '').
     * Variables, backticks, and other special characters are not evaluated.
     */
    private static String escapeForPowerShellLiteral(String str) {
        if (str == null) {
            return "";
        }
        // Remove null characters to prevent string truncation
        str = str.replace("\0", "");
        // In PowerShell single-quoted string literals, single quotes are escaped by doubling them
        str = str.replace("'", "''");
        // Safe newline handling via PowerShell [Environment]::NewLine concatenation
        str = str.replace("\r", "").replace("\n", "' + [Environment]::NewLine + '");
        return str;
    }

//? if !LITE_VERSION {
    public static void toastWithPowershell(String caption, String text) {
        TaskScheduler.schedule(() -> {
            try {
                LoggerUtils.info("[ChatTools] Toast Notified with Powershell.");
                String safeCaption = escapeForPowerShellLiteral(caption);
                String safeText = escapeForPowerShellLiteral(text);

                String script =
                        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;" +
                        "$template = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);" +
                        "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument;" +
                        "$xml.LoadXml($template.GetXml());" +
                        "$texts = $xml.GetElementsByTagName('text');" +
                        "$texts.Item(0).AppendChild($xml.CreateTextNode('" + safeCaption + "')) > $null;" +
                        "$texts.Item(1).AppendChild($xml.CreateTextNode('" + safeText + "')) > $null;" +
                        "$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);" +
                        "$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('Chat Tools Toast');" +
                        "$notifier.Show($toast);";

                String encodedScript = java.util.Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
                ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encodedScript);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LoggerUtils.info("[ChatTools] " + line);
                    }
                }
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Failed to send PowerShell toast notification", e);
            }
        }, 0);
    }
//?}

    public static void toastWithTwoSlices(String caption, String text) {
        LoggerUtils.info("[ChatTools] Toast Notified with Two-Slices.");
        System.setProperty("java.awt.headless", "false");
        TaskScheduler.schedule(() -> {
            com.sshtools.twoslices.Toast.toast(ToastType.INFO, caption, text);
        }, 0);
    }

    private static String truncateWithEllipsis(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
}
