package net.apple70cents.chattools.features.chatnotifier;

import net.apple70cents.chattools.ChatTools;
import net.minecraft.client.MinecraftClient;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class SystemToast {

    public static void toastWithAWT(String caption, String text) {
        ChatTools.LOGGER.info("[ChatTools] Toast Notified with AWT.");
        System.setProperty("java.awt.headless", "false");
        System.out.println("GraphicsEnvironment.isHeadless() = " + GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (GraphicsEnvironment.isHeadless()) System.setProperty("java.awt.headless", "false");
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
            }
        });
    }

    public static void toastWithPowershell(String caption, String text) {
//        if (!Desktop.isDesktopSupported()) {
//            ChatTools.LOGGER.info("[ChatTools] Toast Notified with Powershell, but not isDesktopSupported().");
//            return;
//        }
        try {
            ChatTools.LOGGER.info("[ChatTools] Toast Notified with Powershell.");
            Method getWindowTitleMethod = MinecraftClient.class.getDeclaredMethod("getWindowTitle");
            getWindowTitleMethod.setAccessible(true);
            String command = "powershell.exe -ExecutionPolicy Bypass -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null;$xml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);$xml.GetElementsByTagName('text')[0].AppendChild($xml.CreateTextNode(\\\""
                    + (caption + "`r`n" + text) + "\\\"));$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('"
                    + getWindowTitleMethod.invoke(MinecraftClient.getInstance()) + "');$notifier.Show($toast);\"";
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
