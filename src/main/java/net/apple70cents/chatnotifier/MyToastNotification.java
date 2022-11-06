package net.apple70cents.chatnotifier;

import javax.swing.*;
import java.awt.*;

public class MyToastNotification {
    static {
        System.setProperty("java.awt.headless", "false");
        System.out.println("GraphicsEnvironment.isHeadless() = " + GraphicsEnvironment.isHeadless());
    }
    public static void toast(String caption,String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(GraphicsEnvironment.isHeadless())
                    System.setProperty("java.awt.headless", "false");
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "ChatNotifier");
                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    e.printStackTrace();
                }
                trayIcon.displayMessage(caption,text,TrayIcon.MessageType.NONE);
                tray.remove(trayIcon);
//                System.exit(0);
            }
        });
    }

}
