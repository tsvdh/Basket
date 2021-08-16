package core;

import app.BasketApp;
import common.ExternalPropertiesHandler;
import common.PathHandler;
import core.library.Info;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;
import javafx.concurrent.Task;
import main.Settings;
import net.lingala.zip4j.ZipFile;
import util.Version;

import static java.lang.Math.pow;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static util.url.URLConstructor.makeURL;

public class InstallTask extends Task<Void> {

    private final String name;
    private final String description;
    private final URL iconURL;
    private final URL githubHome;
    private final Version stable;
    private final Version latest;

    public InstallTask(String name, String description, URL iconURL, URL githubHome, Version stable, Version latest) {
        this.name = name;
        this.description = description;
        this.iconURL = iconURL;
        this.githubHome = githubHome;
        this.stable = stable;
        this.latest = latest;
    }

    @Override
    protected Void call() {
        updateMessage("Setting up");

        Path appHomePath = Path.of(PathHandler.getAppHomePath(name));
        URL githubURL = makeURL(toGithubAddress(githubHome.toString(), stable));
        Path imagePath = Path.of(appHomePath + "/image.zip");

        URLConnection urlConnection;
        FileOutputStream imageOutStream = null;
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
                    .setProperty(Info.description, description)
                    .setProperty(Info.current_version, stable)
                    .setProperty(Info.stable_version, stable)
                    .setProperty(Info.latest_version, latest)
                    .save();

            // download icon
            Files.copy(iconStream, Path.of(appHomePath + "/icon.png"));

            // download image
            updateMessage("downloading");

            imageOutStream = new FileOutputStream(imagePath.toString());

            long current = 0;
            long total = urlConnection.getContentLengthLong();
            String totalMB = toMB(total);

            byte[] buffer = new byte[(int) pow(10, 3)]; // 1KB buffer
            int bytesRead;

            while ((bytesRead = imageInStream.read(buffer)) != -1) {
                imageOutStream.write(buffer, 0, bytesRead);
                current += bytesRead;

                updateMessage("downloading: " + toMB(current) + " / " + totalMB + " MB" + "\r");
                updateProgress(current, total);
            }
            imageOutStream.close();

            // extract image
            updateMessage("extracting");
            ZipFile zipFile = new ZipFile(imagePath.toString());
            zipFile.extractAll(appHomePath.toString());
            // delete zip
            Files.delete(imagePath);

            // on success, add app name to settings
            ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
            StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);
            strings.add(name);
            settingsHandler.setProperty(Settings.installed_apps, strings).save();
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

    private static String toGithubAddress(String githubHome, Version version) {
        return githubHome + "/releases/download/" + version.toString() + "/image.zip";
    }

    private static String toMB(long bytes) {
        double MB = pow(10, 6);

        DecimalFormat decimalFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
        double megaBytes = bytes / MB;

        return decimalFormat.format(megaBytes);
    }

    private static void deletePathAndContent(Path path) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
