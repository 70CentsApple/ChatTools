package net.apple70cents.chattools.features.notifier;

import com.sshtools.twoslices.ToastType;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;

import javax.swing.*;
import java.awt.*;

public class Toast {
    public static void work(String text) {
        final String TITLE = TextUtils.trans("texts.toast.title").getString();
        switch ((String) ChatTools.CONFIG.get("notifier.Toast.Mode")) {
            case "AWT":
                toastWithAWT(TITLE, text);
                break;
            case "TWO_SLICES":
                toastWithTwoSlices(TITLE, text);
                break;
            default:
                return;
        }
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

    public static void toastWithTwoSlices(String caption, String text) {
        LoggerUtils.info("[ChatTools] Toast Notified with Two-Slices.");
        System.setProperty("java.awt.headless", "false");
        com.sshtools.twoslices.Toast.toast(ToastType.INFO, caption, text);
    }
}
