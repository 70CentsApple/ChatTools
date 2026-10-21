package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.common.ConfigUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author 70CentsApple
 */
public class DownloadUtils {
//? if !LITE_VERSION {
    private static final String DOWNLOAD_SITE = "https://70centsapple.top/download/chat_tools/download.php?version=250805&file=";
    public static final Path STORAGE_DIR = Path.of(
//? if FABRIC {
            /*net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir()
*///?} elif NEOFORGE {
            net.neoforged.fml.loading.FMLPaths.GAMEDIR.get()
//?}
                    .toString(), "chattools");
    private static final Map<String, String> WIN_7_FILENAMES = Map.of("icon", "icon.ico", "toastExe", "toast-win7.exe");
    private static final Map<String, String> WIN_10_FILENAMES = Map.of("icon", "icon.ico", "toastExe", "toast-win10.exe");
    private static final String OS = System.getProperty("os.name");
    // whether it is the first scan, or we have no successful scans.
    private static boolean shouldCheckMD5OnJudgingReadiness = true;
//?}

    public static boolean shouldCheckIfFullyReady() {
//? if !LITE_VERSION {
        return ConfigUtils.getBoolean("notifier.Toast.Enabled") && "ADDON".equals(
                ConfigUtils.getString("notifier.Toast.Mode"));
//?} else {
        /*return false;
*///?}
    }

//? if !LITE_VERSION {
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
                        LoggerUtils.error("[ChatTools] Error checking MD5 for addon file", e);
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
                return WIN_10_FILENAMES;
            default:
                return null;
        }
    }

    public static void startDownload() {
        startDownloadWithCallback(null);
    }

    public static void startDownloadWithCallback(QuadConsumer<String, Integer, Integer, Integer> processSupplier) {
        Runnable downloadRunnable = () -> {
            switch (OS) {
                case "Windows 7":
                case "Windows 8.1":
                    downloadAndCheckFiles(WIN_7_FILENAMES.values().stream().toList(), STORAGE_DIR, processSupplier);
                    return;
                case "Windows 10":
                case "Windows 11":
                    downloadAndCheckFiles(WIN_10_FILENAMES.values().stream().toList(), STORAGE_DIR, processSupplier);
                    return;
                default:
                    LoggerUtils.warn(String.format("[ChatTools] Addon toast is not available on %s", OS));
            }
        };
        TaskScheduler.schedule(downloadRunnable, 0);
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
                LoggerUtils.error("[ChatTools] Error occurred while downloading \"" + url + "\".", e);
            }
        }
        LoggerUtils.info("[ChatTools] Addons are fully downloaded and ready.");
    }
//?}

//? if !LITE_VERSION {
    private static void downloadFile(String fileName, Path targetPath, QuadConsumer<String, Integer, Integer, Integer> processSupplier) throws Exception {
        URL url = new URL(DOWNLOAD_SITE + fileName);
        HttpURLConnection connection = createOfficialConnection(url);

        int fileSize = connection.getContentLength();
        try (InputStream in = connection.getInputStream(); FileOutputStream fos = new FileOutputStream(
                targetPath.toFile())) {

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


    private static String downloadMD5(String md5Url) throws Exception {
        URL url = new URL(md5Url);
        HttpURLConnection connection = createOfficialConnection(url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }
//?}

    // Let's Encrypt, see https://letsencrypt.org/certs/isrgrootx1.pem
    private static final String ISRG_ROOT_X1_PEM =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw\n" +
            "TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh\n" +
            "cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4\n" +
            "WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu\n" +
            "ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY\n" +
            "MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc\n" +
            "h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+\n" +
            "0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U\n" +
            "A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW\n" +
            "T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH\n" +
            "B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC\n" +
            "B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv\n" +
            "KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn\n" +
            "OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn\n" +
            "jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw\n" +
            "qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI\n" +
            "rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV\n" +
            "HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq\n" +
            "hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL\n" +
            "ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ\n" +
            "3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK\n" +
            "NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5\n" +
            "ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur\n" +
            "TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC\n" +
            "jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc\n" +
            "oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq\n" +
            "4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA\n" +
            "mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d\n" +
            "emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=\n" +
            "-----END CERTIFICATE-----";

    // Google Trust Services, see https://i.pki.goog/r1.pem
    private static final String GTS_ROOT_R1_PEM =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIFVzCCAz+gAwIBAgINAgPlk28xsBNJiGuiFzANBgkqhkiG9w0BAQwFADBHMQsw\n" +
            "CQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZpY2VzIExMQzEU\n" +
            "MBIGA1UEAxMLR1RTIFJvb3QgUjEwHhcNMTYwNjIyMDAwMDAwWhcNMzYwNjIyMDAw\n" +
            "MDAwWjBHMQswCQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZp\n" +
            "Y2VzIExMQzEUMBIGA1UEAxMLR1RTIFJvb3QgUjEwggIiMA0GCSqGSIb3DQEBAQUA\n" +
            "A4ICDwAwggIKAoICAQC2EQKLHuOhd5s73L+UPreVp0A8of2C+X0yBoJx9vaMf/vo\n" +
            "27xqLpeXo4xL+Sv2sfnOhB2x+cWX3u+58qPpvBKJXqeqUqv4IyfLpLGcY9vXmX7w\n" +
            "Cl7raKb0xlpHDU0QM+NOsROjyBhsS+z8CZDfnWQpJSMHobTSPS5g4M/SCYe7zUjw\n" +
            "TcLCeoiKu7rPWRnWr4+wB7CeMfGCwcDfLqZtbBkOtdh+JhpFAz2weaSUKK0Pfybl\n" +
            "qAj+lug8aJRT7oM6iCsVlgmy4HqMLnXWnOunVmSPlk9orj2XwoSPwLxAwAtcvfaH\n" +
            "szVsrBhQf4TgTM2S0yDpM7xSma8ytSmzJSq0SPly4cpk9+aCEI3oncKKiPo4Zor8\n" +
            "Y/kB+Xj9e1x3+naH+uzfsQ55lVe0vSbv1gHR6xYKu44LtcXFilWr06zqkUspzBmk\n" +
            "MiVOKvFlRNACzqrOSbTqn3yDsEB750Orp2yjj32JgfpMpf/VjsPOS+C12LOORc92\n" +
            "wO1AK/1TD7Cn1TsNsYqiA94xrcx36m97PtbfkSIS5r762DL8EGMUUXLeXdYWk70p\n" +
            "aDPvOmbsB4om3xPXV2V4J95eSRQAogB/mqghtqmxlbCluQ0WEdrHbEg8QOB+DVrN\n" +
            "VjzRlwW5y0vtOUucxD/SVRNuJLDWcfr0wbrM7Rv1/oFB2ACYPTrIrnqYNxgFlQID\n" +
            "AQABo0IwQDAOBgNVHQ8BAf8EBAMCAYYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4E\n" +
            "FgQU5K8rJnEaK0gnhS9SZizv8IkTcT4wDQYJKoZIhvcNAQEMBQADggIBAJ+qQibb\n" +
            "C5u+/x6Wki4+omVKapi6Ist9wTrYggoGxval3sBOh2Z5ofmmWJyq+bXmYOfg6LEe\n" +
            "QkEzCzc9zolwFcq1JKjPa7XSQCGYzyI0zzvFIoTgxQ6KfF2I5DUkzps+GlQebtuy\n" +
            "h6f88/qBVRRiClmpIgUxPoLW7ttXNLwzldMXG+gnoot7TiYaelpkttGsN/H9oPM4\n" +
            "7HLwEXWdyzRSjeZ2axfG34arJ45JK3VmgRAhpuo+9K4l/3wV3s6MJT/KYnAK9y8J\n" +
            "ZgfIPxz88NtFMN9iiMG1D53Dn0reWVlHxYciNuaCp+0KueIHoI17eko8cdLiA6Ef\n" +
            "MgfdG+RCzgwARWGAtQsgWSl4vflVy2PFPEz0tv/bal8xa5meLMFrUKTX5hgUvYU/\n" +
            "Z6tGn6D/Qqc6f1zLXbBwHSs09dR2CQzreExZBfMzQsNhFRAbd03OIozUhfJFfbdT\n" +
            "6u9AWpQKXCBfTkBdYiJ23//OYb2MI3jSNwLgjt7RETeJ9r/tSQdirpLsQBqvFAnZ\n" +
            "0E6yove+7u7Y/9waLd64NnHi/Hm3lCXRSHNboTXns5lndcEZOitHTtNCjv0xyBZm\n" +
            "2tIMPNuzjsmhDYAPexZ3FL//2wmUspO8IFgV6dtxQ/PeEMMA3KgqlbbC1j+Qa3bb\n" +
            "bP6MvPJwNQzcmRk13NfIRmPVNnGuV/u3gm3c\n" +
            "-----END CERTIFICATE-----";

    private static SSLSocketFactory officialSslSocketFactory;
    private static boolean officialSslSocketFactoryInitialized = false;

    private static SSLSocketFactory insecureSslSocketFactory;
    private static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = (hostname, session) -> true;

    private static synchronized SSLSocketFactory getOfficialSslSocketFactory() {
        if (officialSslSocketFactoryInitialized) {
            return officialSslSocketFactory;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            defaultTmf.init((KeyStore) null);
            int systemCount = 0;
            for (TrustManager tm : defaultTmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    X509Certificate[] issuers = ((X509TrustManager) tm).getAcceptedIssuers();
                    if (issuers != null) {
                        for (X509Certificate cert : issuers) {
                            keyStore.setCertificateEntry("sys_ca_" + (systemCount++), cert);
                        }
                    }
                }
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate isrgCert = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(ISRG_ROOT_X1_PEM.getBytes(StandardCharsets.UTF_8)));
            keyStore.setCertificateEntry("bundled_isrg_root_x1", isrgCert);

            X509Certificate gtsCert = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(GTS_ROOT_R1_PEM.getBytes(StandardCharsets.UTF_8)));
            keyStore.setCertificateEntry("bundled_gts_root_r1", gtsCert);

            TrustManagerFactory customTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            customTmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, customTmf.getTrustManagers(), new SecureRandom());
            officialSslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Failed to create official SSL socket factory, falling back to system default", e);
            try {
                officialSslSocketFactory = SSLContext.getDefault().getSocketFactory();
            } catch (Exception ignored) {
            }
        }
        officialSslSocketFactoryInitialized = true;
        return officialSslSocketFactory;
    }

    private static synchronized SSLSocketFactory getInsecureSslSocketFactory() throws Exception {
        if (insecureSslSocketFactory == null) {
            TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new SecureRandom());
            insecureSslSocketFactory = sc.getSocketFactory();
        }
        return insecureSslSocketFactory;
    }

    /**
     * Creates an HttpURLConnection for downloading official mod assets.
     * Uses strict SSL certificate verification backed by system CAs plus bundled
     * ISRG Root X1 and GTS Root R1 certificates, guaranteeing compatibility across
     * all JRE versions without compromising security.
     */
    public static HttpURLConnection createOfficialConnection(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            SSLSocketFactory factory = getOfficialSslSocketFactory();
            if (factory != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
            }
        }
        connection.setRequestProperty("User-Agent", "Chrome/138.0.0.0");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        return connection;
    }

    /**
     * Creates an HttpURLConnection for user-configured endpoints (e.g. placeholders).
     * Bypasses SSL certificate and hostname verification ONLY for local / private network addresses
     * (e.g. localhost, 127.0.0.1, 192.168.x.x, 10.x.x.x) or when explicitly requested,
     * while enforcing standard secure TLS validation for all public endpoints.
     */
    public static HttpURLConnection createUserConnection(URL url, boolean forceInsecure) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            boolean bypassSsl = forceInsecure || isLocalOrPrivateAddress(url.getHost());
            if (bypassSsl) {
                httpsConnection.setSSLSocketFactory(getInsecureSslSocketFactory());
                httpsConnection.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
            }
        }
        return connection;
    }

    /**
     * Checks if the given hostname is a local loopback, link-local, or private network address.
     */
    public static boolean isLocalOrPrivateAddress(String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        String cleanHost = host.trim().toLowerCase();
        if (cleanHost.equals("localhost") || cleanHost.endsWith(".localhost") || cleanHost.endsWith(".local")) {
            return true;
        }
        try {
            InetAddress addr = InetAddress.getByName(cleanHost);
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress();
        } catch (Exception e) {
            return cleanHost.equals("127.0.0.1") || cleanHost.equals("::1")
                    || cleanHost.startsWith("10.")
                    || cleanHost.startsWith("192.168.")
                    || cleanHost.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
        }
    }

//? if !LITE_VERSION {
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
//?}
}