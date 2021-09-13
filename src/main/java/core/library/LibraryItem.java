package core.library;

import basket.api.common.ExternalPropertiesHandler;
import basket.api.common.PathHandler;
import basket.api.prebuilt.Message;
import basket.api.util.Version;
import core.App;
import core.Basket;
import core.InstallTask;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import main.Settings;
import org.jetbrains.annotations.Nullable;

import static basket.api.common.FileHandler.deletePathAndContent;
import static java.lang.Runtime.getRuntime;
import static util.ThreadHandler.execute;

public class LibraryItem extends AnchorPane {

    private final String appName;
    private final Path appHomePath;

    private InstallTask stableInstallTask;
    private InstallTask experimentalInstallTask;
    private InstallTask currentInstallTask;

    private Version current;

    boolean useExperimentalOldValue;

    public LibraryItem(String appName, @Nullable App app) {
        super();

        URL url = getClass().getResource("/fxml/library_item.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.appName = appName;
        this.appHomePath = PathHandler.getAppHomePath(appName);

        nameLabel.setText(appName);

        try (InputStream in = Files.newInputStream(appHomePath.resolve("icon.png"))) {
            icon.setImage(new Image(in));
        } catch (IOException ignored) {}

        if (app == null) {
            updateButton.setDisable(true);
            useExperimentalButton.setDisable(true);
            return;
        }

        ExternalPropertiesHandler infoHandler;

        try {
            infoHandler = new ExternalPropertiesHandler(
                    appHomePath.resolve("info.properties"), null);

            if (!appName.equals(app.getName())) {
                throw new RuntimeException("Wrong document");
            }
        }
        catch (IOException | RuntimeException e) {
            new Message(e.getMessage() + ", updating disabled for " + appName, true);
            updateButton.setDisable(true);
            useExperimentalButton.setDisable(true);
            return;
        }

        Version stable = app.getStable();
        Version experimental = app.getExperimental();

        URL iconURL = app.getIconAddress();
        URL githubHome = app.getGithubHome();

        this.stableInstallTask = new InstallTask(appName, iconURL, githubHome, stable,
                false, true);
        this.experimentalInstallTask = new InstallTask(appName, iconURL, githubHome, experimental,
                true, true);

        current = (Version) infoHandler.getProperty(AppInfo.current_version);

        useExperimentalOldValue = (boolean) infoHandler.getProperty(AppInfo.use_experimental);
        useExperimentalButton.setSelected(useExperimentalOldValue);

        if (useExperimentalOldValue) {
            this.currentInstallTask = experimentalInstallTask;
        } else {
            this.currentInstallTask = stableInstallTask;
        }

        setUpdateButton();
    }

    public void setUpdateButton() {
        // switching type of version, so update should always be allowed
        if (useExperimentalOldValue != useExperimentalButton.isSelected()) {
            updateButton.setDisable(false);
        }
        // regular update, allowed if version is newer
        else {
            boolean isNewer = currentInstallTask.getWantedVersion().compareTo(current) > 0;
            updateButton.setDisable(!isNewer);
        }
    }

    @FXML
    public Label nameLabel;

    @FXML
    public ImageView icon;

    @FXML
    public Button launchButton;

    @FXML
    public void launch() {
        Path executable = PathHandler.getAppHomePath(appName).resolve("image/bin/" + appName + ".bat");

        if (!Files.exists(executable)) {
            // TODO: invoke repair
            launchButton.setDisable(true);
            new Message("This app is broken", true);
            return;
        }

        Process process;
        try {
            process = getRuntime().exec(executable.toString());
        } catch (IOException e) {
            e.printStackTrace();
            new Message("Unable to launch the app", true);
            return;
        }

        launchButton.setText("Stop");
        launchButton.setOnAction(event -> {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        });

        process.onExit().thenRun(() -> Platform.runLater(() -> {
            launchButton.setText("Launch");
            launchButton.setOnAction(event -> launch());
        }));
    }

    @FXML
    public MenuItem updateButton;

    @FXML
    public void update() {
        currentInstallTask.bindBars(progressBar, loadingBar);

        progressLabel.textProperty().bind(currentInstallTask.messageProperty());

        controlHBox.setVisible(false);
        installHBox.setVisible(true);

        currentInstallTask.setOnSucceeded(event -> {
            controlHBox.setVisible(true);
            installHBox.setVisible(false);

            if (currentInstallTask.getValue()) {
                Basket.getInstance().loadLibrary();
            }
        });

        execute(currentInstallTask);
    }

    @FXML
    public CheckMenuItem useExperimentalButton;

    @FXML
    public void swapVersion() {
        if (currentInstallTask == stableInstallTask) {
            currentInstallTask = experimentalInstallTask;
        } else {
            currentInstallTask = stableInstallTask;
        }
        setUpdateButton();
        updateButton.fire();
    }

    @FXML
    public void remove() {
        // un-register app
        try {
            Settings.removeApp(appName);
        } catch (IOException e) {
            new Message(e.getMessage(), true);
            return;
        }

        // refresh store and library
        Platform.runLater(() -> {
            Basket.getInstance().loadStore();
            Basket.getInstance().controller.libraryVBox.getChildren().remove(this);
        });

        // Delete app. Doesn't matter if this fails, the folder will be deleted at install anyway
        try {
            deletePathAndContent(appHomePath);
        } catch (IOException ignored) {}
    }

    @FXML
    public HBox controlHBox;

    @FXML
    public HBox installHBox;

    @FXML
    public Label progressLabel;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public ProgressBar loadingBar;
}
