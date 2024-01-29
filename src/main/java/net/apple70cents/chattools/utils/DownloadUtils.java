package net.apple70cents.chattools.utils;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author 70CentsApple
 */
public class DownloadUtils {
    private static final String DOWNLOAD_SITE = "https://70centsapple.top/download/chat_tools/download.php?";
    public static final Path STORAGE_DIR = Path.of(FabricLoader.getInstance().getGameDir().toString(), "chattools");
    private static final Map<String, String> WIN_7_FILENAMES = Map.of("icon", "icon.ico", "toastExe", "toast-win7.exe");
    private static final Map<String, String> WIN_10_11_FILENAMES = Map.of("icon", "icon.ico", "toastExe", "toast-win10-and-win11.exe");
    private static final String OS = System.getProperty("os.name");
    // whether it is the first scan, or we have no successful scans.
    private static boolean shouldCheckMD5OnJudgingReadiness = true;

    public static boolean checkIfFullyReady() {
        Map<String, String> fileNames = getFileNamesMap();
        if (fileNames == null) {
            return false;
        }
        for (String fileName : fileNames.values()) {
            File file = Path.of(STORAGE_DIR.toString(), fileName).toFile();
            if (!file.exists()) {
                // This time, we don't find the file.
                // Next time, we should check md5.
                shouldCheckMD5OnJudgingReadiness = true;
                return false;
            } else {
                if (shouldCheckMD5OnJudgingReadiness) {
                    try {
                        String md5Url = DOWNLOAD_SITE + fileName + ".md5";
                        String md5 = downloadMD5(md5Url);
                        if (!checkMD5(file.toPath(), md5)) {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        shouldCheckMD5OnJudgingReadiness = false;
        return true;
    }

    public static Map<String, String> getFileNamesMap() {
        switch (OS) {
            case "Windows 7":
            case "Windows 8.1":
                return WIN_7_FILENAMES;
            case "Windows 10":
            case "Windows 11":
                return WIN_10_11_FILENAMES;
            default:
                return null;
        }
    }

    public static void startDownload() {
        startDownloadWithCallback(null);
    }

    public static void startDownloadWithCallback(QuadConsumer<String, Integer, Integer, Integer> processSupplier) {
        Runnable runnable = () -> {
            switch (OS) {
                case "Windows 7":
                case "Windows 8.1":
                    downloadAndCheckFiles(WIN_7_FILENAMES.values().stream().toList(), STORAGE_DIR, processSupplier);
                    return;
                case "Windows 10":
                case "Windows 11":
                    downloadAndCheckFiles(WIN_10_11_FILENAMES.values().stream().toList(), STORAGE_DIR, processSupplier);
                    return;
                default:
                    LoggerUtils.warn(String.format("[ChatTools] Addon toast is not available on %s", OS));
            }
        };
        // Start the file download in a new thread
        Thread downloadThread = new Thread(runnable, "ChatTools-Download-Thread");
        downloadThread.start();
    }

    public static void downloadAndCheckFiles(List<String> filesToDownload, Path downloadDirectory, QuadConsumer<String, Integer, Integer, Integer> processSupplier) {
        // mkdir if the folder does not exist
        if (!downloadDirectory.toFile().exists()) {
            downloadDirectory.toFile().mkdirs();
        }
        for (String fileName : filesToDownload) {
            String url = DOWNLOAD_SITE + fileName;
            try {
                String md5Url = url + ".md5";
                String md5 = downloadMD5(md5Url);
                Path filePath = Path.of(downloadDirectory.toString(), fileName);
                if (filePath.toFile().exists()) {
                    // we have this file, let's check its MD5
                    if (checkMD5(filePath, md5)) {
                        LoggerUtils.info("[ChatTools] " + fileName + " md5 checker passed, skip downloading it.");
                        continue;
                    }
                }
                // download the file
                downloadFile(fileName, filePath, processSupplier);
                // download its MD5 and check
                if (checkMD5(filePath, md5)) {
                    LoggerUtils.info("[ChatTools] " + fileName + " was downloaded successfully.");
                }
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error occurred while downloading \"" + url + "\".");
                e.printStackTrace();
            }
        }
        LoggerUtils.info("[ChatTools] Addons are fully downloaded and ready.");
    }

    private static void downloadFile(String fileName, Path targetPath, QuadConsumer<String, Integer, Integer, Integer> processSupplier) throws IOException {
        URL url = new URL(DOWNLOAD_SITE + fileName);
        URLConnection connection = url.openConnection();
        int fileSize = connection.getContentLength();
        try (InputStream in = connection.getInputStream(); FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            int prevPercentage = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                int percentage = (int) (totalBytesRead * 100 / fileSize);

                if (percentage != prevPercentage) {
                    System.out.print("\rDownloading " + fileName + ": " + percentage + "%");
                    prevPercentage = percentage;
                }
                if (processSupplier != null) {
                    processSupplier.accept(fileName, percentage, (int) totalBytesRead / 1024, fileSize / 1024);
                }
            }
            System.out.println("\rDownloading " + fileName + ": Completed");
        }
    }


    private static String downloadMD5(String md5Url) throws IOException {
        URL url = new URL(md5Url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.readLine();
        }
    }

    private static boolean checkMD5(Path filePath, String md5) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(filePath)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) != -1) {
                ;
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().equalsIgnoreCase(md5);
    }
}