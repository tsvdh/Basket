package core.library;

import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.prebuilt.Message;
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
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitMenuButton;
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
import server.common.model.app.Release.Type;
import server.common.model.user.AppUsage;

import static java.lang.Integer.parseInt;
import static util.ThreadHandler.execute;
import static util.Util.signEqual;

public class LibraryItem extends AnchorPane {

    @Getter
    private final App app;
    private final byte @Nullable [] iconArray;

    private InstallInfo installInfo;

    public LibraryItem(String appId) throws IOException {
        super();

        Path appInfoPath = PathHandler.getAppLibraryPath(appId).resolve("app.json");
        this.app = JSONHandler.read(appInfoPath, App.class).getObject();

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

        buttons = List.of(launchButton, installButton, refreshButton, optionsButton);
        icons = List.of(cloudStatusIcon, diskStatusIcon, errorIcon);

        bindSceneProperties();

        refresh();

        nameLabel.setText(app.getName());

        if (iconArray != null) {
            appIcon.setImage(new Image(new ByteArrayInputStream(iconArray)));
        }
    }

    // --- Init

    @FXML
    public Label nameLabel;

    @FXML
    public ImageView appIcon;

    // ---
    // --- Install

    @FXML
    public FlowPane controlPane;

    @FXML
    public FlowPane installPane;

    @FXML
    public Label progressLabel;

    @FXML
    public ProgressBar loadingBar;

    @FXML
    public ProgressBar progressBar;

    // ---
    // --- Refresh

    @FXML
    public ProgressIndicator refreshIndicator;

    @FXML
    public Label lastUsedLabel;

    @FXML
    public Label timeUsedLabel;

    @FXML
    public Button launchButton;

    @FXML
    public SplitMenuButton installButton;

    @FXML
    public MenuItem installExperimentalButton;

    @FXML
    public HBox statusHBox;

    @FXML
    public Button refreshButton;

    @FXML
    public ImageView errorIcon;

    @FXML
    public ImageView diskStatusIcon;

    @FXML
    public ImageView cloudStatusIcon;

    @FXML
    public Label updateLabel;

    @FXML
    public MenuButton optionsButton;

    @FXML
    public MenuItem updateButton;

    @FXML
    public CheckMenuItem experimentalCheckItem;

    @FXML
    public CheckMenuItem stableCheckItem;

    // ---

    private List<Node> buttons;
    private List<Node> icons;

    @FXML
    public void refresh() {
        // TODO: read session from disk
        refresh(null);
    }

    public void refresh(Session session) {
        buttons.forEach(button -> button.setDisable(true));
        refreshIndicator.setVisible(true);

        var refreshTask = new LibraryRefreshTask(app, iconArray, session);

        refreshTask.setOnSucceeded(event -> {
            buttons.forEach(button -> button.setDisable(false));
            icons.forEach(icon -> icon.setVisible(false));

            showPersistentInfo();

            Set<Result> results = refreshTask.getValue();

            if (results.contains(Result.NOT_RUNNABLE)) {
                buttons.forEach(button -> button.setDisable(true));
                refreshButton.setDisable(false);

                errorIcon.setVisible(true);
            }

            if (results.contains(Result.RUNNABLE)) {
                try {
                    Path infoPath = PathHandler.getAppLibraryPath(app.getId()).resolve("_info.json");
                    installInfo = JSONHandler.read(infoPath, InstallInfo.class).getObject();
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
                    diskStatusIcon.setVisible(true);
                }
                if (results.contains(Result.UPLOAD_FAILED)) {
                    cloudStatusIcon.setVisible(true);
                }
            }

            refreshIndicator.setVisible(false);
        });

        execute(refreshTask);
    }

    @FXML
    public void launch() {
        // Path executable = Util.getAppLibraryPath(appId).resolve();
        // if (!Files.exists(executable)) {
        //     // TODO: invoke repair
        //     launchButton.setDisable(true);
        //     new Message("This app is broken", true);
        //     return;
        // }
        //
        // Process process;
        // try {
        //     process = getRuntime().exec(executable.toString());
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     new Message("Unable to launch the app", true);
        //     return;
        // }
        //
        // launchButton.setText("Stop");
        // launchButton.setOnAction(event -> {
        //     process.descendants().forEach(ProcessHandle::destroy);
        //     process.destroy();
        // });
        //
        // long startTime = System.currentTimeMillis();
        //
        // process.onExit().thenRun(() -> Platform.runLater(() -> {
        //     launchButton.setText("Launch");
        //     launchButton.setOnAction(event -> launch());
        //
        //     try {
        //         ExternalPropertiesHandler persistentInfoHandler = new ExternalPropertiesHandler(
        //                 PathHandler.getDataFolderOfAppPath(appName).resolve("info.properties"), null);
        //         Duration timeUsed = (Duration) persistentInfoHandler.getProperty(OfflineAppInfo.time_used);
        //
        //         long millisUsed = System.currentTimeMillis() - startTime;
        //         timeUsed = timeUsed.plusMillis(millisUsed);
        //         LocalDate lastUsed = LocalDate.now();
        //
        //         persistentInfoHandler
        //                 .setProperty(OfflineAppInfo.time_used, timeUsed)
        //                 .setProperty(OfflineAppInfo.last_used, lastUsed)
        //                 .save();
        //
        //         showPersistentInfo(timeUsed, lastUsed);
        //     }
        //     catch (IOException e) {
        //         new Message(e.toString(), true);
        //     }
        // }));
    }

    @FXML
    public void installStable() {
        executeInstallTask(app.getStable());
    }

    @FXML
    public void installExperimental() {
        executeInstallTask(app.getExperimental());
    }

    @FXML
    public void update() {
        if (installInfo.getCurrentRelease().getType() == Type.STABLE) {
            executeInstallTask(app.getStable());
        } else {
            executeInstallTask(app.getExperimental());
        }
    }

    @FXML
    public void swapToExperimental() {
        executeInstallTask(app.getExperimental());
    }

    @FXML
    public void swapToStable() {
        executeInstallTask(app.getStable());
    }

    @FXML
    public void remove() {
        // TODO: add functionality to install task
        new Message("This does not work yet", false);
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

        refreshIndicator.managedProperty().bind(refreshIndicator.visibleProperty());

        refreshButton.visibleProperty().bind(statusHBox.hoverProperty());

        updateLabel.visibleProperty().bind(optionsButton.disableProperty().not().and(updateButton.disableProperty().not()));

        experimentalCheckItem.disableProperty().bind(stableCheckItem.disableProperty().not());

        controlPane.visibleProperty().bind(installPane.visibleProperty().not());

        statusHBox.visibleProperty().bind(
                    cloudStatusIcon.visibleProperty()
                .or(diskStatusIcon.visibleProperty())
                .or(errorIcon.visibleProperty()));
    }

    private void executeInstallTask(Release release) {
        var task = new InstallTask(app.getId(), release);

        progressLabel.textProperty().bind(task.messageProperty());

        task.progressProperty().addListener((observable, oldValue, newValue) -> {

            if (signEqual(oldValue, newValue)) {
                return; // no change as same bar should be kept
            }

            boolean loading = newValue.floatValue() == -1;

            loadingBar.setVisible(loading);
            progressBar.setVisible(!loading);
        });

        progressBar.progressProperty().bind(task.progressProperty());

        installPane.setVisible(true);

        task.setOnSucceeded(event -> {
            installPane.setVisible(false);

            if (task.getValue() == InstallTask.Result.INSTALL_CHANGED) {
                refresh();
            }
        });

        execute(task);
    }
}
