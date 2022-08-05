package core.store;

import basket.api.handlers.FileHandler;
import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.prebuilt.Message;
import core.library.PersistentAppInfo;
import core.library.TempAppInfo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import main.Settings;
import server.ServerConnectionException;
import server.ServerHandler;
import server.ServerHandler.LibraryAction;
import server.common.FileName;
import server.common.model.app.App;

import static basket.api.app.BasketApp.getSettingsHandler;

@RequiredArgsConstructor
public class AcquireTask extends Task<Boolean> {

    private final App app;

    @Override
    protected Boolean call() {
        // generate data
        var tempAppInfo = new TempAppInfo(false, null);
        var persistentAppInfo = new PersistentAppInfo(Duration.ZERO, OffsetDateTime.now(), false);

        Path dataPath = PathHandler.getAppDataPath(app.getId());
        Path libraryPath = PathHandler.getAppLibraryPath(app.getId());

        InputStream iconStream;
        try {
            iconStream = ServerHandler.getInstance()
                    .getDownloadStream(app.getId(), FileName.ICON).body();
        } catch (ServerConnectionException e) {
            Platform.runLater(() -> new Message("Could not download from server", true));
            return false;
        }

        JSONHandler<PersistentAppInfo> persistentInfoHandler;

        var settings = getSettingsHandler().getConvertedObject(Settings.class);

        // create new directories and files
        try {
            Files.createDirectories(dataPath);
            Files.createDirectories(libraryPath);

            Files.copy(iconStream, libraryPath.resolve(FileName.ICON));

            JSONHandler.create(libraryPath.resolve("_info.json"), tempAppInfo);
            persistentInfoHandler = JSONHandler.create(dataPath.resolve("_info.json"), persistentAppInfo);

            JSONHandler.create(dataPath.resolve("app.json"), app);

            settings.getAcquiredApps().add(app.getId());
            getSettingsHandler().save();
        }
        catch (IOException e) {
            settings.getAcquiredApps().remove(app.getId());

            try {
                FileHandler.deletePathAndContent(dataPath);
                FileHandler.deletePathAndContent(libraryPath);
            }
            catch (IOException extraException) {
                System.err.println("Could not clean up: " + extraException.getMessage());
            }

            Platform.runLater(() -> new Message(e.getMessage(), true));

            return false;
        }

        // try to notify server and save notified
        try {
            ServerHandler.getInstance().modifyLibrary(app.getId(), LibraryAction.add);

            persistentAppInfo.setServerNotifiedOfAcquisition(true);
            try {
                persistentInfoHandler.save();
            } catch (IOException ignored) {
                // will be handled at install
            }
        } catch (ServerConnectionException ignored) {
            // will be handled at install
        }

        return true;
    }
}
