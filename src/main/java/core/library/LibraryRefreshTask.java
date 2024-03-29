package core.library;

import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.prebuilt.Message;
import core.Basket;
import core.library.LibraryRefreshTask.Result;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jetbrains.annotations.Nullable;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.AppSession;
import server.common.FileName;
import server.common.model.app.App;

import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// guarantee file existence, and save memory state to server or disk
public class LibraryRefreshTask extends Task<Set<Result>> {

    public enum Result {
        RUNNABLE,
        NOT_RUNNABLE,
        WRITE_FAILED,
        UPLOAD_FAILED
    }

    private final App app;
    private final byte[] iconArray;
    private final AppSession session;

    public LibraryRefreshTask(App app, byte @Nullable [] iconArray, @Nullable AppSession session) {
        this.app = app;
        this.iconArray = iconArray;
        this.session = session;
    }

    @Override
    protected Set<Result> call() {
        // generate data
        var installInfo = new InstallInfo(false, null);
        var offlineAppInfo = new OfflineAppInfo(new LinkedList<>());

        Path dataPath = PathHandler.getAppDataPath(app.getId());
        Path libraryPath = PathHandler.getAppLibraryPath(app.getId());
        Path iconPath = libraryPath.resolve(FileName.ICON);
        Path installInfoPath = libraryPath.resolve("_info.json");
        Path offlineAppInfoPath = dataPath.resolve("_info.json");
        Path appInfoPath = libraryPath.resolve("app.json");

        JSONHandler<OfflineAppInfo> offlineInfoHandler = null;

        boolean writeFailed = false;

        // create new directories and files
        try {
            Files.createDirectories(dataPath);
            Files.createDirectories(libraryPath);

            if (iconArray != null) {
                Files.copy(new ByteArrayInputStream(iconArray), iconPath, REPLACE_EXISTING);
            }

            JSONHandler.create(installInfoPath, installInfo, false);
            offlineInfoHandler = JSONHandler.create(offlineAppInfoPath, offlineAppInfo, false);

            JSONHandler.create(appInfoPath, app, true);
        }
        catch (IOException e) {
            writeFailed = true;
        }

        boolean uploaded = true;

        List<AppSession> sessions = new LinkedList<>();
        if (session != null) {
            sessions.add(session);
        }
        if (offlineInfoHandler != null) {
            var diskSessions = List.copyOf(offlineInfoHandler.getObject().getSessions());
            sessions.addAll(diskSessions);

            try {
                offlineInfoHandler.getObject().getSessions().clear();
                offlineInfoHandler.save();
            } catch (IOException e) {
                sessions.removeAll(diskSessions);
                uploaded = false;
            }
        }

        if (!sessions.isEmpty()) {
            boolean shouldRefreshUser = true;
            try {
                ServerHandler.getInstance().notifyAppSessions(app.getId(), sessions);
            } catch (ServerConnectionException serverException) {
                uploaded = false;
                shouldRefreshUser = false;

                if (offlineInfoHandler != null) {
                    try {
                        offlineInfoHandler.getObject().getSessions().addAll(sessions);
                        offlineInfoHandler.save();
                    } catch (IOException ioException) {

                        // if nothing is on disk to upload later, upload is complete
                        if (offlineInfoHandler.getObject().getSessions().isEmpty()) {
                            uploaded = true;
                        }

                        offlineInfoHandler.getObject().getSessions().removeAll(sessions);
                        Platform.runLater(() -> new Message("Session(s) could not be saved", true));
                    }
                }
            }

            if (shouldRefreshUser) {
                try {
                    Basket.getInstance().refreshUser();
                } catch (ServerConnectionException e) {
                    System.err.println("Could not refresh user");
                }
            }
        }

        boolean filesPresent = exists(dataPath)
                && exists(libraryPath)
                && exists(iconPath)
                && exists(installInfoPath)
                && exists(offlineAppInfoPath)
                && exists(appInfoPath);

        Set<Result> results = new HashSet<>();

        if (!filesPresent) {
            results.add(Result.NOT_RUNNABLE);
        } else {
            results.add(Result.RUNNABLE);
        }

        if (writeFailed) {
            results.add(Result.WRITE_FAILED);
        }

        if (!uploaded) {
            results.add(Result.UPLOAD_FAILED);
        }

        return results;
    }
}
