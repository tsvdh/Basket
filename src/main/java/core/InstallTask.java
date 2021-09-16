package core;

import basket.api.common.ExternalPropertiesHandler;
import basket.api.prebuilt.Message;
import basket.api.util.Version;
import core.library.AppInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import main.Settings;
import net.lingala.zip4j.ZipFile;
import util.Util;

import static basket.api.common.FileHandler.copyPathAndContent;
import static basket.api.common.FileHandler.deletePathAndContent;
import static basket.api.util.url.URLConstructor.makeURL;
import static java.lang.Math.pow;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static util.Util.signEqual;

public class InstallTask extends Task<Boolean> {

    private final String appName;
    private final URL iconURL;
    private final URL githubHome;
    private final Version wantedVersion;
    private final boolean experimental;
    private final boolean update;

    public InstallTask(String appName, URL iconURL, URL githubHome, Version wantedVersion,
                       boolean experimental, boolean update) {
        this.appName = appName;
        this.iconURL = iconURL;
        this.githubHome = githubHome;
        this.wantedVersion = wantedVersion;
        this.experimental = experimental;
        this.update = update;
    }

    public Version getWantedVersion() {
        return wantedVersion;
    }

    public void bindBars(ProgressBar progressBar, ProgressBar loadingBar) {
        this.progressProperty().addListener((observable, oldValue, newValue) -> {

            if (signEqual(oldValue, newValue)) {
                return; // no change as same bar should be kept
            }

            boolean loading = newValue.floatValue() == -1;

            loadingBar.setVisible(loading);
            progressBar.setVisible(!loading);
        });

        progressBar.progressProperty().bind(this.progressProperty());
    }

    @Override
    protected Boolean call() {
        Path appHomePath = Util.getAppLibraryPath(appName);
        Path backupPath = appHomePath.resolveSibling(appName + "_backup");

        URL githubURL = makeURL(toGithubAddress(githubHome, wantedVersion));
        Path imagePath = appHomePath.resolve("image.zip");

        updateMessage("Setting up");

        if (!update) {
            updateProgress(0, 1);
        }
        else {
            updateProgress(-1, 1);
            try {
                // backup files
                copyPathAndContent(appHomePath, backupPath);
            } catch (IOException e) {
                new Message("Could not create backup", true);
                return false;
            }
        }

        InputStream iconStream = null;
        InputStream imageInStream = null;
        OutputStream imageOutStream = null;
        try {
            URLConnection urlConnection = githubURL.openConnection();
            iconStream = iconURL.openStream();
            imageInStream = urlConnection.getInputStream();

            // create or clean app directory
            deletePathAndContent(appHomePath);
            Files.createDirectory(appHomePath);

            // store app info
            ExternalPropertiesHandler infoHandler = new ExternalPropertiesHandler(
                    appHomePath.resolve("info.properties"), null);
            infoHandler.setProperty(AppInfo.name, appName)
                    .setProperty(AppInfo.current_version, wantedVersion)
                    .setProperty(AppInfo.use_experimental, experimental)
                    .save();

            // download icon
            Files.copy(iconStream, appHomePath.resolve("icon.png"));

            // download image
            updateMessage("Downloading");

            imageOutStream = Files.newOutputStream(imagePath);

            long current = 0;
            long total = urlConnection.getContentLengthLong();
            String totalMB = toMB(total);

            byte[] buffer = new byte[(int) pow(10, 4)]; // 10KB buffer
            int bytesRead;

            while ((bytesRead = imageInStream.read(buffer)) != -1) {
                imageOutStream.write(buffer, 0, bytesRead);
                current += bytesRead;

                updateMessage("Downloading: " + toMB(current) + " / " + totalMB + " MB");
                updateProgress(current, total);
            }
            imageOutStream.close();

            // extract image
            updateMessage("Extracting");
            updateProgress(-1, 1);

            ZipFile zipFile = new ZipFile(imagePath.toString());
            zipFile.extractAll(appHomePath.toString());
            // delete zip
            Files.delete(imagePath);

            if (!update) {
                // register app
                Settings.addApp(appName);
            }
        }
        catch (IOException | RuntimeException e) {
            if (!update) {
                Platform.runLater(() -> new Message("Install failed: " + e, true));
                try {
                    deletePathAndContent(appHomePath);
                } catch (IOException ignored) {}
            } else {
                updateMessage("Restoring");
                Platform.runLater(() -> new Message("Update failed: " + e, true));
                try {
                    copyPathAndContent(backupPath, appHomePath);
                } catch (IOException backupFail) {
                    try {
                        Platform.runLater(() -> new Message("Backup failed: " + e, true));
                        Settings.removeApp(appName);
                        // TODO: remove from GUI
                    } catch (IOException fatal) {
                        Platform.runLater(() -> new Message(fatal + ", " + appName
                                + " is broken. Try to remove the app later", true));
                    }
                }
                try {
                    deletePathAndContent(backupPath);
                } catch (IOException ignored) {}
            }
            return false;
        }
        finally {
            closeQuietly(iconStream);
            closeQuietly(imageInStream);
            closeQuietly(imageOutStream);
        }

        return true;
    }

    public static String toGithubAddress(URL githubHome, Version version) {
        return githubHome + "/releases/download/" + version + "/image.zip";
    }

    public static String toMB(long bytes) {
        double MB = pow(10, 6);

        DecimalFormat decimalFormat = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
        double megaBytes = bytes / MB;

        return decimalFormat.format(megaBytes);
    }
}
