package core;

import app.BasketApp;
import common.ExternalPropertiesHandler;
import common.PathHandler;
import core.library.Info;
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
import javafx.concurrent.Task;
import main.Settings;
import net.lingala.zip4j.ZipFile;
import util.Version;

import static common.FileHandler.deletePathAndContent;
import static java.lang.Math.pow;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static util.url.URLConstructor.makeURL;

public class InstallTask extends Task<Void> {

    private final String name;
    private final URL iconURL;
    private final URL githubHome;
    private final Version wantedVersion;

    public InstallTask(String name, URL iconURL, URL githubHome, Version wantedVersion) {
        this.name = name;
        this.iconURL = iconURL;
        this.githubHome = githubHome;
        this.wantedVersion = wantedVersion;
    }

    @Override
    protected Void call() {
        updateMessage("Setting up");
        updateProgress(0, 1);

        Path appHomePath = Path.of(PathHandler.getAppHomePath(name));
        URL githubURL = makeURL(toGithubAddress(githubHome.toString(), wantedVersion));
        Path imagePath = Path.of(appHomePath + "/image.zip");

        URLConnection urlConnection;
        OutputStream imageOutStream = null;
        try (
                InputStream iconStream = iconURL.openStream();
                InputStream imageInStream = (urlConnection = githubURL.openConnection()).getInputStream();
        ) {
            // create or clean app directory
            deletePathAndContent(appHomePath);
            Files.createDirectory(appHomePath);

            // store app info
            ExternalPropertiesHandler infoHandler = new ExternalPropertiesHandler(
                    appHomePath + "/info.properties", null);
            infoHandler.setProperty(Info.name, name)
                    .setProperty(Info.current_version, wantedVersion)
                    .setProperty(Info.use_latest, false)
                    .save();

            // download icon
            Files.copy(iconStream, Path.of(appHomePath + "/icon.png"));

            // download image
            updateMessage("Downloading");

            imageOutStream = Files.newOutputStream(imagePath);

            long current = 0;
            long total = urlConnection.getContentLengthLong();
            String totalMB = toMB(total);

            byte[] buffer = new byte[(int) pow(10, 3)]; // 1KB buffer
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

            // register app
            ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
            StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);
            strings.add(name);
            settingsHandler.setProperty(Settings.installed_apps, strings);
        }
        catch (IOException | RuntimeException e) {
            try {
                deletePathAndContent(appHomePath);
            } catch (IOException ignored) {}
        } finally {
            closeQuietly(imageOutStream);
        }

        return null;
    }

    public static String toGithubAddress(String githubHome, Version version) {
        return githubHome + "/releases/download/" + version.toString() + "/image.zip";
    }

    public static String toMB(long bytes) {
        double MB = pow(10, 6);

        DecimalFormat decimalFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
        double megaBytes = bytes / MB;

        return decimalFormat.format(megaBytes);
    }


}
