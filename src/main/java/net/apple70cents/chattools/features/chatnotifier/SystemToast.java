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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SystemToast {
    public static final String OS = System.getProperty("os.name");
    public static List<String> DOWNLOADS;

    public static void downloadAddonToast(TriConsumer<Integer, Integer, Integer> processSupplier) {
        String dir = FabricLoader.getInstance().getGameDir() + "/chattools/";
        try {
            // mkdir if the folder does not exist
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            // Delete if exists
            for (String file : DOWNLOADS) {
                if (new File(dir + file).exists()) {
                    ChatTools.LOGGER.warn(String.format("[ChatTools] found existing %s, deleting it.", file));
                    new File(dir + file).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Start the file download in a new thread
        Thread downloadThread = new Thread(() -> {
            try {
                int bytesRead;
                int totalBytesRead = 0;
                int fileSizeInTotal = 0;
                Queue<String> downloadQueue = new LinkedList<String>();
                for (String file : DOWNLOADS) {
                    HttpURLConnection connection = (HttpURLConnection) new URL("https://gitlab.com/70CentsApple/70CentsApple.Gitlab.io/-/raw/main/files/ChatTools/" + file).openConnection();
                    fileSizeInTotal += connection.getContentLength();
                    downloadQueue.add(file);
                }
                while (!downloadQueue.isEmpty()) {
                    String file = downloadQueue.poll();
                    HttpURLConnection connection = (HttpURLConnection) new URL("https://gitlab.com/70CentsApple/70CentsApple.Gitlab.io/-/raw/main/files/ChatTools/" + file).openConnection();
                    FileOutputStream fileOutputStream = new FileOutputStream(Path.of(dir + file).toFile());
                    ChatTools.LOGGER.info(String.format("[ChatTools] Downloading %s to %s", file, dir));
                    connection.connect();
                    try (ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream()); FileChannel fileChannel = fileOutputStream.getChannel()) {
                        byte[] buffer = new byte[4096];
                        while ((bytesRead = readableByteChannel.read(ByteBuffer.wrap(buffer))) != -1) {
                            fileChannel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                            totalBytesRead += bytesRead;
                            // Calculate download progress
                            int progress = (int) ((double) totalBytesRead / fileSizeInTotal * 100);
                            processSupplier.accept(progress, totalBytesRead / 1024, fileSizeInTotal / 1024);
                            System.out.print("\rProgress: " + progress + "%");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ChatTools-Download-Thread");
        downloadThread.start();
    }

    public static boolean isAddonToastReady(boolean shouldCheckOnline) {
        try {
            for (String fileName : DOWNLOADS) {
                File file = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", fileName);
                if (!file.exists()) {
                    ChatTools.LOGGER.warn(String.format("[ChatTools] Addon Toast is not ready, %s does not exist.", fileName));
                    return false;
                }
                if (!shouldCheckOnline) {
                    continue;
                }
                URL downloadUrl = new URL("https://gitlab.com/70CentsApple/70CentsApple.Gitlab.io/-/raw/main/files/ChatTools/" + fileName);
                HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
                int fileSize = connection.getContentLength();
                if (fileSize != 0) {
                    if (fileSize != file.length()) {
                        ChatTools.LOGGER.warn(String.format("[ChatTools] Addon Toast is not ready, %s has a mismatched size. (local:%d, remote:%d, urlPath:%s)", fileName, file.length(), fileSize, downloadUrl.getPath()));
                        return false;
                    }
                } else {
                    // if it could not connect to the url, we assert it is ready.
                    ChatTools.LOGGER.warn("[ChatTools] Addon Toast is maybe ready, but we can't connect to Internet for checking.");
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // if there is something wrong, we assert it is ready.
            ChatTools.LOGGER.warn("[ChatTools] Addon Toast is maybe ready, but an exception was caught.");
            return true;
        }
        return true;
    }

    public static void toastWithAddon(String caption, String text) {
        if (DOWNLOADS.isEmpty()) {
            ChatTools.LOGGER.warn(String.format("[ChatTools] Addon toast is not available on %s", OS));
            return;
        }
        ChatTools.LOGGER.info("[ChatTools] Trying to toast with addon.");
        if (!isAddonToastReady(false)) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.translatable("key.chattools.addonToastNotReady"));
            }
            return;
        }
        Thread thread = new Thread(() -> {
            String toastExeFileName;
            String iconFileName = "ChatToolsIcon.ico";
            if (OS.contains("Windows 7") || OS.contains("Windows 8.1")) {
                toastExeFileName = "toast-win7.exe";
            } else if (OS.contains("Windows 10") || OS.contains("Windows 11")) {
                toastExeFileName = "toast-win10-and-win11.exe";
            } else {
                ChatTools.LOGGER.warn(String.format("[ChatTools] Addon toast is not available on %s", OS));
                return;
            }

            if (OS.contains("Windows 7") || OS.contains("Windows 8.1") || OS.contains("Windows 10") || OS.contains("Windows 11")) {
                File file = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", toastExeFileName);
                File iconFile = new File(FabricLoader.getInstance().getGameDir() + "/chattools/", iconFileName);
                String command = String.format("%s %s %s %s", '"' + file.toString() + '"', '"' + caption + '"', '"' + text.replace("\n", "\\n") + '"', '"' + iconFile.toString() + '"');
                ProcessBuilder builder = new ProcessBuilder(command);
                System.out.println("builder.command() = " + builder.command());
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
            }
        }, "ChatTools-Toast-Thread");
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
        try {
            ChatTools.LOGGER.info("[ChatTools] Toast Notified with Powershell.");
            final String COMMAND_TEMPLATE = "powershell.exe -ExecutionPolicy Bypass -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null;$xml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);$xml.GetElementsByTagName('text')[0].AppendChild($xml.CreateTextNode('%s'));$toast = [Windows.UI.Notifications.ToastNotification]::new($xml);$notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('%s');$notifier.Show($toast);\"";
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
