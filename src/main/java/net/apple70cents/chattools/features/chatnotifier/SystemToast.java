package net.apple70cents.chattools.features.chatnotifier;

import net.apple70cents.chattools.ChatTools;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.util.TriConsumer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class SystemToast {
    public static void downloadPythonToast(TriConsumer<Integer, Integer, Integer> processSupplier) {
        String dir = FabricLoader.getInstance().getGameDir() + "/chattools/";
        try {
            // mkdir if the folder does not exist
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            // Delete if exists
            if (new File(dir + "ChatToolsToast.exe").exists()) {
                ChatTools.LOGGER.warn("[ChatTools] found existing ChatToolsToast file, deleting it.");
                new File(dir + "ChatToolsToast.exe").delete();
            }
            if (new File(dir + "ChatToolsIcon.ico").exists()) {
                ChatTools.LOGGER.warn("[ChatTools] found existing ChatToolsIcon file, deleting it.");
                new File(dir + "ChatToolsIcon.ico").delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Start the file download in a new thread
        Thread downloadThread = new Thread(() -> {
            try {
                FileOutputStream fileOutputStream1 = new FileOutputStream(Path.of(dir + "ChatToolsToast.exe").toFile());
                HttpURLConnection connection1 = (HttpURLConnection) new URL("https://70centsapple.github.io/files/ChatToolsToast.exe").openConnection();
                FileOutputStream fileOutputStream2 = new FileOutputStream(Path.of(dir + "ChatToolsIcon.ico").toFile());
                HttpURLConnection connection2 = (HttpURLConnection) new URL("https://70centsapple.github.io/files/ChatToolsIcon.ico").openConnection();

                int bytesRead;
                int totalBytesRead = 0;

                int fileSize = connection1.getContentLength() + connection2.getContentLength();
                ChatTools.LOGGER.info(String.format("[ChatTools] Downloading ChatToolsToast.exe to %s", dir));
                connection1.connect();
                try (ReadableByteChannel readableByteChannel = Channels.newChannel(connection1.getInputStream()); FileChannel fileChannel = fileOutputStream1.getChannel()) {
                    byte[] buffer = new byte[4096];
                    while ((bytesRead = readableByteChannel.read(ByteBuffer.wrap(buffer))) != -1) {
                        fileChannel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                        totalBytesRead += bytesRead;
                        // Calculate download progress
                        int progress = (int) ((double) totalBytesRead / fileSize * 100);
                        processSupplier.accept(progress, totalBytesRead / 1024, fileSize / 1024);
                        System.out.print("\rProgress: " + progress + "%");
                    }
                }
                ChatTools.LOGGER.info(String.format("[ChatTools] Downloading ChatToolsIcon.ico to %s", dir));
                connection2.connect();
                try (ReadableByteChannel readableByteChannel = Channels.newChannel(connection2.getInputStream()); FileChannel fileChannel = fileOutputStream2.getChannel()) {
                    byte[] buffer = new byte[4096];
                    while ((bytesRead = readableByteChannel.read(ByteBuffer.wrap(buffer))) != -1) {
                        fileChannel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                        totalBytesRead += bytesRead;
                        // Calculate download progress
                        int progress = (int) ((double) totalBytesRead / fileSize * 100);
                        processSupplier.accept(progress, totalBytesRead / 1024, fileSize / 1024);
                        System.out.print("\rProgress: " + progress + "%");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        downloadThread.start();
    }

    public static boolean isPythonToastReady() {
        var file = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", "ChatToolsToast.exe");
        if (!file.exists()) {
            return false;
        }
        try {
            URL downloadUrl = new URL("https://70centsapple.github.io/files/ChatToolsToast.exe");
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            int fileSize = connection.getContentLength();
            if (fileSize != 0){
                return fileSize == file.length();
            } else {
                // if it could not connect to the url, we assert it is ready.
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // if there is something wrong, we assert it is ready.
            return true;
        }
    }

    public static void toastWithPython(String caption, String text) {
        if (!isPythonToastReady()) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.translatable("key.chattools.pythonToastNotReady"),true);
            }
            return;
        }
        Thread thread = new Thread(() -> {
            var file = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", "ChatToolsToast.exe");
            var iconFile = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", "ChatToolsIcon.ico");
            String command = String.format("%s %s %s %s", '"' + file.toString() + '"', '"' + caption + '"', '"' + text.replace("\n", "\\n") + '"', iconFile);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            try {
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
        });
        thread.start();
    }

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
