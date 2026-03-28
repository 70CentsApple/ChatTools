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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Toast {
    private static final ExecutorService TOAST_EXECUTOR_THREAD_POOL = Executors.newCachedThreadPool();
    private static String text;
    private static final CircuitBreakerExecutor toastExecutor = CircuitBreakerExecutor.of(() -> {
        final String TITLE = TextUtils.trans("texts.toast.title").getString();
        String truncatedText = truncateWithEllipsis(text, 250);
        switch ((String) ConfigUtils.get("notifier.Toast.Mode")) {
            case "AWT":
                toastWithAWT(TITLE, truncatedText);
                break;
            case "POWERSHELL":
                toastWithPowershell(TITLE, truncatedText);
                break;
            case "ADDON":
                toastWithAddon(TITLE, truncatedText);
                break;
            case "TWO_SLICES":
                toastWithTwoSlices(TITLE, truncatedText);
                break;
            default:
                return;
        }
    }).setMaxLimitPerSecond(() -> ((Number) ConfigUtils.get("general.CircuitBreaker.ToastThreshold")).intValue())
    .setFailsafeFunction(() -> {
        ConfigUtils.set("notifier.Toast.Enabled", false);
        int threshold = ((Number) ConfigUtils.get("general.CircuitBreaker.ToastThreshold")).intValue();
        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold));
        MessageUtils.sendToActionbar(TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold));
        LoggerUtils.warn("[ChatTools] " + TextUtils.trans("texts.CircuitBreaker.exceed.Toast", threshold).getString());
    }).setFailsafeJudgement(() -> (Boolean) ConfigUtils.get("notifier.Toast.Enabled"));

    public static void work(String text1) {
        text = text1;
        toastExecutor.run();
    }

    public static void toastWithAddon(String caption, String text) {
        if (!DownloadUtils.checkIfFullyReady()) {
            MessageUtils.sendToActionbar(TextUtils.trans("texts.toast.failure"));
            return;
        }
        LoggerUtils.info("[ChatTools] Trying to toast with addon.");
        TOAST_EXECUTOR_THREAD_POOL.submit(() -> {
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
                e.printStackTrace();
            }
        });
    }

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
                e.printStackTrace();
            }
            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.NONE);
            tray.remove(trayIcon);
        });
    }

    public static void toastWithPowershell(String caption, String text) {
        TOAST_EXECUTOR_THREAD_POOL.submit(() -> {
            try {
                LoggerUtils.info("[ChatTools] Toast Notified with Powershell.");
                final String COMMAND_TEMPLATE = "powershell.exe -ExecutionPolicy Bypass -Command \"" +
                        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;" +
                        "$template = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);" +
                        "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument;" + "$xml.LoadXml($template.GetXml());" +
                        "$texts = $xml.GetElementsByTagName('text');" + "$texts.Item(0).AppendChild($xml.CreateTextNode('%s')) > $null;" +
                        "$texts.Item(1).AppendChild($xml.CreateTextNode('%s')) > $null;" + "$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);" +
                        "$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('%s');" + "$notifier.Show($toast);\"";
                String command = String.format(COMMAND_TEMPLATE, caption, text.replace("\n", "'+\\\"`r`n\\\"+'"), "Chat Tools Toast");
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LoggerUtils.info("[ChatTools] " + line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void toastWithTwoSlices(String caption, String text) {
        LoggerUtils.info("[ChatTools] Toast Notified with Two-Slices.");
        System.setProperty("java.awt.headless", "false");
        TOAST_EXECUTOR_THREAD_POOL.submit(() -> {
            com.sshtools.twoslices.Toast.toast(ToastType.INFO, caption, text);
        });
    }

    private static String truncateWithEllipsis(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
}
