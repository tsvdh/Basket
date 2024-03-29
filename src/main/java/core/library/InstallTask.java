package core.library;

import basket.api.handlers.FileHandler;
import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.prebuilt.Confirmation;
import basket.api.prebuilt.Message;
import core.library.InstallTask.Result;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.FileName;
import server.common.model.app.Release;

import static basket.api.handlers.FileHandler.copyPathAndContent;
import static basket.api.handlers.FileHandler.deletePathAndContent;
import static java.lang.Math.pow;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static util.Util.toMB;

@RequiredArgsConstructor
public class InstallTask extends Task<Result> {

    public enum Result {
        INSTALL_CHANGED,
        INSTALL_KEPT
    }

    private final String appId;
    private final Release release;

    @Override
    protected Result call() {
        updateMessage("Setting up");
        updateProgress(0, 1);

        Path appHomePath = PathHandler.getAppLibraryPath(appId);
        Path imagePath = appHomePath.resolve("image");
        Path imageZipPath = appHomePath.resolve("image.zip");
        Path backupPath = appHomePath.resolve("backup_image");

        Path tempInfoPath = appHomePath.resolve("_info.json");

        // handle info file
        JSONHandler<InstallInfo> installInfoHandler;
        try {
            installInfoHandler = JSONHandler.read(tempInfoPath, InstallInfo.class);
        } catch (IOException e) {
            Platform.runLater(() -> new Message("Could not read file: " + e.getMessage(), true));
            return Result.INSTALL_KEPT;
        }

        var installInfo = installInfoHandler.getObject();

        // backup files
        if (installInfo.isInstalled()) {
            updateMessage("Backing up");
            updateProgress(-1, 1);
            try {
                // backup files
                copyPathAndContent(imagePath, backupPath);
            } catch (IOException e) {
                Platform.runLater(() -> new Message("Could not create backup", true));
                return Result.INSTALL_KEPT;
            }
        }

        // get download stream
        String fileToDownload = switch (release.getType()) {
            case STABLE -> FileName.STABLE;
            case EXPERIMENTAL -> FileName.EXPERIMENTAL;
        };

        HttpResponse<InputStream> response;
        try {
            response = ServerHandler.getInstance().getDownloadStream(appId, fileToDownload);
        } catch (ServerConnectionException e) {
            try {
                FileHandler.deletePathAndContent(backupPath);
            } catch (IOException ignored) {}

            Platform.runLater(() -> new Message("Could not download from server", true));
            return Result.INSTALL_KEPT;
        }

        InputStream imageInStream = null;
        OutputStream imageOutStream = null;
        try {
            imageInStream = response.body();

            // set up app directory
            deletePathAndContent(imageZipPath);
            deletePathAndContent(imagePath);

            // download image
            updateMessage("Downloading");

            imageOutStream = Files.newOutputStream(imageZipPath);

            long current = 0;
            long total = Long.parseLong(response.headers().map().get("Content-Length").get(0));
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

            var env = new HashMap<String, String>();
            env.put("create", "true");

            String zippedDirectoryName;

            try (FileSystem zipFs = FileSystems.newFileSystem(imageZipPath, env);
                 Stream<Path> pathStream = Files.list(zipFs.getPath(""))) {

                List<Path> pathList = pathStream.toList();
                if (pathList.size() == 1 && Files.isDirectory(pathList.get(0))) {
                    zippedDirectoryName = pathList.get(0).getFileName().toString();
                } else {
                    zippedDirectoryName = null;
                }
            }

            ZipFile zipFile = new ZipFile(imageZipPath.toString());
            // zipped folder
            if (zippedDirectoryName != null) {
                zipFile.extractAll(appHomePath.toString());
                Files.move(appHomePath.resolve(zippedDirectoryName), imagePath);
            }
            // zipped files
            else {
                zipFile.extractAll(imagePath.toString());
            }

            try {
                // delete zip
                Files.delete(imageZipPath);
            } catch (IOException ignored) {
                // ignore as zip remaining is not important
                System.err.println("Could not delete zip");
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();

            if (!installInfo.isInstalled()) {
                try {
                    deletePathAndContent(imagePath);
                    deletePathAndContent(imageZipPath);
                } catch (IOException deleteFail) {
                    deleteFail.printStackTrace();
                }

                Platform.runLater(() -> new Message("Install failed: " + e, true));
            }
            else {
                updateMessage("Restoring");
                Platform.runLater(() -> new Message("Update failed: " + e, true));
                try {
                    copyPathAndContent(backupPath, imagePath);
                } catch (IOException backupFail) {
                    try {
                        Platform.runLater(() -> new Message("Backup failed: " + e, true));

                        installInfo.setInstalled(false);
                        installInfoHandler.save();
                    }
                    catch (IOException fatal) {
                        Platform.runLater(() -> new Message("This app is broken. Try to uninstall the app later", true));
                        return Result.INSTALL_KEPT;
                    }
                    return Result.INSTALL_CHANGED;
                }
            }

            return Result.INSTALL_KEPT;
        }
        finally {
            closeQuietly(imageInStream);
            closeQuietly(imageOutStream);

            try {
                deletePathAndContent(backupPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // update info about install
        if (!installInfo.isInstalled()) {
            installInfo.setInstalled(true);
        }
        installInfo.setCurrentRelease(release);

        // try saving info, if user stops trying the install-tasks are kept and can be tried again later
        while (true) {
            try {
                installInfoHandler.save();
                break;
            } catch (IOException e) {
                String question = "Could not save info, please free %s. Try again?".formatted(tempInfoPath);
                if (!new Confirmation(question).getResult()) {
                    return Result.INSTALL_KEPT;
                }
            }
        }

        return Result.INSTALL_CHANGED;
    }
}
