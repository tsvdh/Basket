package core.store;

import basket.api.app.BasketApp;
import basket.api.util.Version;
import core.App;
import core.Basket;
import core.InstallTask;
import core.StringQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import main.Settings;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class StoreItem extends AnchorPane {

    private InstallTask installTask;

    public StoreItem(App app) {
        super();

        URL fxml_url = getClass().getResource("/fxml/store_item.fxml");
        FXMLLoader loader = new FXMLLoader(fxml_url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        nameLabel.setText(app.getName());
        descriptionLabel.setText(app.getDescription());

        URL url = app.getIconAddress();

        try (InputStream in = url.openStream()) {
            icon.setImage(new Image(in));
        }
        catch (IOException ignored) {}

        if (installed()) {
            installButton.setDisable(true);
            installButton.setText("Installed");
            return;
        }

        URL githubHome = app.getGithubHome();

        Version stable = app.getStable();

        installTask = new InstallTask(app.getName(), url, githubHome, stable,
                false, false);
    }

    private boolean installed() {
        StringQueue strings = (StringQueue) BasketApp.getSettingsHandler().getProperty(Settings.installed_apps);
        return strings.contains(nameLabel.getText());
    }

    @FXML
    public Label nameLabel;

    @FXML
    public Label descriptionLabel;

    @FXML
    public ImageView icon;

    @FXML
    public Button installButton;

    @FXML
    public void install() {
        installTask.bindBars(progressBar, loadingBar);

        progressLabel.textProperty().bind(installTask.messageProperty());

        installButton.setVisible(false);
        installHBox.setVisible(true);

        installTask.setOnSucceeded(stateEvent -> {
            installButton.setVisible(true);
            installHBox.setVisible(false);


            if (installTask.getValue()) {
                installButton.setDisable(true);
                installButton.setText("Installed");
                Basket.getInstance().loadLibrary();
            }
        });

        newSingleThreadExecutor().execute(installTask);
    }

    @FXML
    public HBox installHBox;

    @FXML
    public Label progressLabel;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public ProgressBar loadingBar;
}
