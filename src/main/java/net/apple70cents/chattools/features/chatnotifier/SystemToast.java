package net.apple70cents.chattools.features.chatnotifier;

import net.apple70cents.chattools.ChatTools;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SystemToast {

    public static void toastWithAWT(String caption, String text) {
        ChatTools.LOGGER.info("[ChatTools] Toast Notified with AWT.");
        System.setProperty("java.awt.headless", "false");
        ChatTools.LOGGER.warn(String.format("[ChatTools] Set java.awt.headless to %s.", GraphicsEnvironment.isHeadless()));
        SwingUtilities.invokeLater(() -> {
            if (GraphicsEnvironment.isHeadless()) {
                System.setProperty("java.awt.headless", "false");
                ChatTools.LOGGER.warn(String.format("[ChatTools] GraphicsEnvironment.isHeadless() was true, but now it is set to %s.", GraphicsEnvironment.isHeadless()));
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
//        if (!Desktop.isDesktopSupported()) {
//            ChatTools.LOGGER.info("[ChatTools] Toast Notified with Powershell, but not isDesktopSupported().");
//            return;
//        }
        // FIXME 只有系统消息能被弹窗（其它的会试图弹窗但是因为在后台出于奇妙原因弹不出来）
        try {
            ChatTools.LOGGER.info("[ChatTools] Toast Notified with Powershell.");
            String COMMAND_TEMPLATE = "powershell.exe -ExecutionPolicy Bypass -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null;$xml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);$xml.GetElementsByTagName('text')[0].AppendChild($xml.CreateTextNode('%s'));$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('%s');$notifier.Show($toast);\"";
            String command = String.format(COMMAND_TEMPLATE, (caption + "'+\\\"`r`n\\\"+'" + text.replace("\n", "'+\\\"`r`n\\\"+'")), "Minecraft Chat Tools Mod");
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            // 启动进程
            Process process = builder.start();

            // 获取进程输出流
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));

            // 读取输出
            String line;
            while ((line = reader.readLine()) != null) {
                ChatTools.LOGGER.info(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
