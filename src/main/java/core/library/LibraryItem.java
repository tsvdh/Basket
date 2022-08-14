package core.library;

import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.util.FatalError;
import core.Basket;
import core.library.LibraryRefreshTask.Result;
import core.library.OfflineAppInfo.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.FileName;
import server.common.model.app.App;
import server.common.model.app.Release;
import server.common.model.user.AppUsage;

import static java.lang.Integer.parseInt;
import static util.ThreadHandler.execute;

public class LibraryItem extends AnchorPane {

    @Getter
    private final App app;
    private final byte @Nullable [] iconArray;

    private InstallInfo installInfo;

    public LibraryItem(String appId) throws IOException {
        super();

        Path appInfoPath = PathHandler.getAppLibraryPath(appId).resolve("app.json");
        this.app = new JSONHandler<App>(appInfoPath).getObject();

        this.iconArray = getIconArray(false);

        init();
    }

    public LibraryItem(App app) {
        super();

        this.app = app;

        this.iconArray = getIconArray(true);

        init();
    }

    private byte[] getIconArray(boolean download) {
        byte[] array;

        if (download) {
            try {
                HttpResponse<InputStream> response = ServerHandler.getInstance().getDownloadStream(app.getId(), FileName.ICON);
                int contentLength = parseInt(response.headers().map().get("Content-Length").get(0));
                array = new byte[contentLength];

                IOUtils.read(response.body(), array);
                return array;
            } catch (ServerConnectionException | IOException e) {
                return getIconArray(false);
            }
        }
        else {
            Path iconPath = PathHandler.getAppLibraryPath(app.getId()).resolve(FileName.ICON);

            try {
                return Files.readAllBytes(iconPath);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private void init() {
        URL url = getClass().getResource("/fxml/library_item.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bindSceneProperties();

        refresh();

        nameLabel.setText(app.getName());

        if (iconArray != null) {
            appIcon.setImage(new Image(new ByteArrayInputStream(iconArray)));
        }
    }

    // --- Init

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView appIcon;

    // ---
    // --- Install

    @FXML
    private FlowPane controlPane;

    @FXML
    private FlowPane installPane;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private ProgressBar progressBar;

    // ---
    // --- Refresh

    @FXML
    private Label lastUsedLabel;

    @FXML
    private Label timeUsedLabel;

    @FXML
    private Button launchButton;

    @FXML
    private Button installButton;

    @FXML
    private MenuItem installExperimentalButton;

    @FXML
    private HBox statusHBox;

    @FXML
    private Button refreshButton;

    @FXML
    private ImageView errorIcon;

    @FXML
    private ImageView diskStatusIcon;

    @FXML
    private ImageView cloudStatusIcon;

    @FXML
    private Label updateLabel;

    @FXML
    private MenuButton optionsButton;

    @FXML
    private MenuItem updateButton;

    @FXML
    private CheckMenuItem experimentalCheckItem;

    @FXML
    private CheckMenuItem stableCheckItem;

    // @FXML
    // public void launch() {
    //     Path executable = Util.getAppLibraryPath(appId).resolve();
    //
    //     if (!Files.exists(executable)) {
    //         // TODO: invoke repair
    //         launchButton.setDisable(true);
    //         new Message("This app is broken", true);
    //         return;
    //     }
    //
    //     Process process;
    //     try {
    //         process = getRuntime().exec(executable.toString());
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         new Message("Unable to launch the app", true);
    //         return;
    //     }
    //
    //     launchButton.setText("Stop");
    //     launchButton.setOnAction(event -> {
    //         process.descendants().forEach(ProcessHandle::destroy);
    //         process.destroy();
    //     });
    //
    //     long startTime = System.currentTimeMillis();
    //
    //     process.onExit().thenRun(() -> Platform.runLater(() -> {
    //         launchButton.setText("Launch");
    //         launchButton.setOnAction(event -> launch());
    //
    //         try {
    //             ExternalPropertiesHandler persistentInfoHandler = new ExternalPropertiesHandler(
    //                     PathHandler.getDataFolderOfAppPath(appName).resolve("info.properties"), null);
    //             Duration timeUsed = (Duration) persistentInfoHandler.getProperty(OfflineAppInfo.time_used);
    //
    //             long millisUsed = System.currentTimeMillis() - startTime;
    //             timeUsed = timeUsed.plusMillis(millisUsed);
    //             LocalDate lastUsed = LocalDate.now();
    //
    //             persistentInfoHandler
    //                     .setProperty(OfflineAppInfo.time_used, timeUsed)
    //                     .setProperty(OfflineAppInfo.last_used, lastUsed)
    //                     .save();
    //
    //             showPersistentInfo(timeUsed, lastUsed);
    //         }
    //         catch (IOException e) {
    //             new Message(e.toString(), true);
    //         }
    //     }));
    // }
    //
    // @FXML
    // public void update() {
    //     currentInstallTask.bindBars(progressBar, loadingBar);
    //
    //     progressLabel.textProperty().bind(currentInstallTask.messageProperty());
    //
    //     controlHBox.setVisible(false);
    //     installHBox.setVisible(true);
    //
    //     currentInstallTask.setOnSucceeded(event -> {
    //         controlHBox.setVisible(true);
    //         installHBox.setVisible(false);
    //
    //         if (currentInstallTask.getValue()) {
    //             Basket.getInstance().loadLibrary();
    //         }
    //     });
    //
    //     execute(currentInstallTask);
    // }
    //
    // @FXML
    // public void swapVersion() {
    //     if (currentInstallTask == stableInstallTask) {
    //         currentInstallTask = experimentalInstallTask;
    //     } else {
    //         currentInstallTask = stableInstallTask;
    //     }
    //     setUpdateButton();
    //     updateButton.fire();
    // }
    //
    // @FXML
    // public void remove() {
    //     // un-register app
    //     try {
    //         Settings.removeApp(appName);
    //     } catch (IOException e) {
    //         new Message(e.toString(), true);
    //         return;
    //     }
    //
    //     // refresh store and library
    //     Platform.runLater(() -> {
    //         Basket.getInstance().loadStore();
    //         Basket.getInstance().controller.libraryVBox.getChildren().remove(this);
    //     });
    //
    //     // Delete app. Doesn't matter if this fails, the folder will be deleted at install anyway
    //     try {
    //         deletePathAndContent(appHomePath);
    //     } catch (IOException ignored) {}
    // }

    @FXML
    public void refresh() {
        // TODO: read session from disk
        refresh(null);
    }

    public void refresh(Session session) {
        var refreshTask = new LibraryRefreshTask(app, iconArray, session);

        refreshTask.setOnSucceeded(event -> {
            showPersistentInfo();

            Set<Result> results = refreshTask.getValue();

            if (results.contains(Result.NOT_RUNNABLE)) {
                launchButton.setDisable(true);
                optionsButton.setDisable(true);

                statusHBox.setVisible(true);
                errorIcon.setVisible(true);
            }

            if (results.contains(Result.RUNNABLE)) {
                try {
                    Path infoPath = PathHandler.getAppLibraryPath(app.getId()).resolve("_info.json");
                    installInfo = new JSONHandler<InstallInfo>(infoPath).getObject();
                } catch (IOException e) {
                    // Should not happen, RUNNABLE result ensures all files exist
                    throw new FatalError(e);
                }

                if (!installInfo.isInstalled()) {
                    launchButton.setVisible(false);
                    installButton.setVisible(true);
                    optionsButton.setDisable(true);

                    if (app.getExperimental() == null) {
                        installExperimentalButton.setDisable(true);
                    }
                } else {
                    switch (installInfo.getCurrentRelease().getType()) {
                        case STABLE -> {
                            if (installInfo.getCurrentRelease().getDate()
                                    .equals(app.getStable().getDate())) {
                                updateButton.setDisable(true);
                            }

                            if (app.getExperimental() == null) {
                                experimentalCheckItem.setDisable(true);
                            }

                            stableCheckItem.setSelected(true);
                        }
                        case EXPERIMENTAL -> {
                            if (installInfo.getCurrentRelease().getDate()
                                    .equals(app.getExperimental().getDate())) {
                                updateButton.setDisable(true);
                            }

                            stableCheckItem.setSelected(false);
                        }
                    }
                }

                if (results.contains(Result.WRITE_FAILED)) {
                    statusHBox.setVisible(true);
                    diskStatusIcon.setVisible(true);
                }
                if (results.contains(Result.UPLOAD_FAILED)) {
                    statusHBox.setVisible(true);
                    cloudStatusIcon.setVisible(true);
                }
            }
        });

        execute(refreshTask);
    }

    @FXML
    void launch() {

    }
    @FXML
    void installStable() {

    }
    @FXML
    void installExperimental() {

    }

    @FXML
    void update() {

    }

    @FXML
    void swapToExperimental() {

    }

    @FXML
    void swapToStable() {

    }

    @FXML
    void remove() {

    }

    private void showPersistentInfo() {
        AppUsage appUsage = Basket.getInstance().getUserInfo().getUsageInfo().get(app.getId());

        if (appUsage == null) {
            timeUsedLabel.setText("Time used: 0m");
            lastUsedLabel.setText("Last used: Never");
            return;
        }

        Duration timeUsed = appUsage.getTimeUsed();
        OffsetDateTime lastUsed = appUsage.getLastUsed();

        String used;
        if (timeUsed.toMinutes() > 60) {
            used = timeUsed.toHours() + "h";
        } else {
            used = timeUsed.toMinutes() + "m";
        }
        timeUsedLabel.setText("Time used: " + used);

        if (lastUsed == null) {
            lastUsedLabel.setText("Last used: Never");
        } else {
            OffsetDateTime now = OffsetDateTime.now();
            int dayDifference = now.getDayOfYear() - lastUsed.getDayOfYear();
            String date;

            if (lastUsed.getYear() == now.getYear() && dayDifference <= 1) {
                if (dayDifference == 0) {
                    date = "Today";
                } else {
                    date = "Yesterday";
                }
            } else {
                date = lastUsed.getDayOfMonth() + " " + lastUsed.getMonth().toString().toLowerCase();
            }
            lastUsedLabel.setText("Last used: " + date);
        }
    }

    private void bindSceneProperties() {
        errorIcon.managedProperty().bind(errorIcon.visibleProperty());
        Tooltip.install(errorIcon, new Tooltip("Some files are missing, ensure you have internet and close blocking programs"));

        diskStatusIcon.managedProperty().bind(diskStatusIcon.visibleProperty());
        Tooltip.install(diskStatusIcon, new Tooltip("Could not write to disk, close any blocking programs"));

        cloudStatusIcon.managedProperty().bind(cloudStatusIcon.visibleProperty());
        Tooltip.install(cloudStatusIcon, new Tooltip("Could not upload to server, ensure you have internet access"));

        refreshButton.visibleProperty().bind(statusHBox.hoverProperty());

        updateButton.disableProperty().bind(updateLabel.visibleProperty().not());

        stableCheckItem.disableProperty().bind(experimentalCheckItem.disableProperty().not());
    }

    private void executeInstallTask(Release release) {

    }
}
