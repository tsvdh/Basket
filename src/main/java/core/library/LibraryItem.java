package core.library;

import app.BasketApp;
import common.ExternalPropertiesHandler;
import common.PathHandler;
import core.Basket;
import core.StringQueue;
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
import main.Settings;
import prebuilt.Message;

import static common.FileHandler.deletePathAndContent;
import static java.lang.Runtime.getRuntime;

public class LibraryItem extends AnchorPane {

    private final String appName;
    private final Path appHomePath;

    public LibraryItem(String appName) {
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

        try (InputStream in = Files.newInputStream(Path.of(appHomePath + "/icon.png"))) {
            icon.setImage(new Image(in));
        } catch (IOException ignored) {}

        try {
            ExternalPropertiesHandler infoHandler = new ExternalPropertiesHandler(
                    appHomePath.resolve("info.properties"), null);
            // set update actions

        } catch (IOException e) {
            updateButton.setDisable(true);
            useExperimentalButton.setDisable(true);
            new Message("A file is in use for " + appName + ", updating disabled", true);
        }

        // set launch
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

    }

    @FXML
    public CheckMenuItem useExperimentalButton;

    @FXML
    public void swapVersion() {

    }

    @FXML
    public void remove() {
        // un-register app
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);
        strings.remove(appName);
        settingsHandler.setProperty(Settings.installed_apps, strings);

        // refresh store and library
        Platform.runLater(() -> {
            Basket.getInstance().loadStore();
            Basket.getInstance().loadLibrary();
        });

        // Delete app. Doesn't matter if this fails, the folder will be deleted at install anyway
        try {
            deletePathAndContent(appHomePath);
        } catch (IOException ignored) {}
    }

    @FXML
    public Label progressLabel;

    @FXML
    public ProgressBar progressBar;
}
