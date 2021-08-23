package core.store;

import app.BasketApp;
import core.Basket;
import core.InstallTask;
import core.StringQueue;
import db.DocumentHelper;
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
import org.bson.Document;
import prebuilt.Message;
import util.Version;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class StoreItem extends AnchorPane {

    private boolean valid;
    private InstallTask installTask;

    public StoreItem(Document document) {
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

        this.valid = true;

        DocumentHelper documentHelper = new DocumentHelper(document);
        try {
            nameLabel.setText(documentHelper.getName());
            descriptionLabel.setText(documentHelper.getDescription());

            URL iconURL = documentHelper.getIconURL();

            try (InputStream in = iconURL.openStream()) {
                icon.setImage(new Image(in));
            }
            catch (IOException ignored) {}

            if (installed()) {
                installButton.setDisable(true);
                installButton.setText("Installed");
                return;
            }

            URL githubHome = documentHelper.getGithubHome();

            Version stable = documentHelper.getStableVersion();

            installTask = new InstallTask(nameLabel.getText(), iconURL, githubHome, stable,
                    false, false);

        } // an exception indicates a bad entry in the database, so they are not added to the store
        catch (RuntimeException e) { // TODO: add some kind of alert for dev/admin
            this.valid = false;
        }
    }

    boolean isValid() {
        return valid;
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
            } else {
                new Message("Could not install app", true);
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
