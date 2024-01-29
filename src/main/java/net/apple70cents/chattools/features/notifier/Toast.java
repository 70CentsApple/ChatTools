package net.apple70cents.chattools.features.notifier;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.DownloadUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class Toast {
    public static void work(String text) {
        final String TITLE = TextUtils.trans("texts.toast.title").getString();
        switch ((String) ChatTools.CONFIG.get("notifier.Toast.Mode")) {
            case "AWT":
                toastWithAWT(TITLE, text);
                break;
            case "POWERSHELL":
                toastWithPowershell(TITLE, text);
                break;
            case "ADDON":
                toastWithAddon(TITLE, text);
                break;
            default:
                return;
        }
    }

    public static void toastWithAddon(String caption, String text) {
        if (!DownloadUtils.checkIfFullyReady()) {
            MessageUtils.sendToActionbar(TextUtils.trans("texts.toast.failure"));
            return;
        }
        LoggerUtils.info("[ChatTools] Trying to toast with addon.");
        Runnable runnable = () -> {
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
            try {
                Process process = builder.start();
                // get input stream of the process
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                // logs the results
                String line;
                while ((line = reader.readLine()) != null) {
                    LoggerUtils.info(line);
                }
            } catch (Exception e) {
                MessageUtils.sendToActionbar(TextUtils.trans("texts.toast.failure"));
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable, "ChatTools-Toast-Thread");
        thread.start();
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
        try {
            LoggerUtils.info("[ChatTools] Toast Notified with Powershell.");
            final String COMMAND_TEMPLATE = "powershell.exe -ExecutionPolicy Bypass -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null;$xml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);$xml.GetElementsByTagName('text')[0].AppendChild($xml.CreateTextNode('%s'));$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('%s');$notifier.Show($toast);\"";
            String command = String.format(COMMAND_TEMPLATE, (caption + "'+\\\"`r`n\\\"+'" + text.replace("\n", "'+\\\"`r`n\\\"+'")), "Minecraft Chat Tools Mod");
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            // start process
            Process process = builder.start();

            // gets input stream
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            // reads and logs the result
            String line;
            while ((line = reader.readLine()) != null) {
                LoggerUtils.info(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
