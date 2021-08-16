package core.store;

import app.BasketApp;
import core.InstallTask;
import core.StringQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.event.ActionEvent;
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
import util.url.BadURLException;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static util.url.URLConstructor.makeURL;

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

        try {
            nameLabel.setText(requireNonNull(document.getString("name")));
            descriptionLabel.setText(requireNonNull(document.getString("description")));

            String iconAddress = requireNonNull(document.getString("icon_address"));
            if (!iconAddress.endsWith(".png")) {
                throw new BadURLException();
            }
            URL iconURL = makeURL(iconAddress);

            try (InputStream iconInputStream = iconURL.openStream()) {
                icon.setImage(new Image(iconInputStream));
            }
            catch (IOException ignored) {}

            if (checkInstalledAndSetButton()) return;

            URL githubHome = makeURL(requireNonNull(document.getString("github_home")));

            Document versions = (Document) document.get("versions");
            Version stable = Version.of(versions.getString("stable"));
            Version latest = Version.of(versions.getString("latest"));

            installTask = new InstallTask(nameLabel.getText(), descriptionLabel.getText(),
                    iconURL, githubHome, stable, latest);

        } // an exception indicates a bad entry in the database, so they are not added to the store
        catch (RuntimeException e) { // TODO: add some kind of alert for dev/admin
            this.valid = false;
        }
    }

    boolean isValid() {
        return valid;
    }

    private boolean checkInstalledAndSetButton() {
        StringQueue strings = (StringQueue) BasketApp.getSettingsHandler().getProperty(Settings.installed_apps);
        if (strings.contains(nameLabel.getText())) {
            installButton.setDisable(true);
            installButton.setText("Installed");
            return true;
        }
        return false;
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
    public void install(ActionEvent event) {
        progressBar.progressProperty().bind(installTask.progressProperty());
        progressLabel.textProperty().bind(installTask.messageProperty());

        installButton.setVisible(false);
        installHBox.setVisible(true);

        installTask.setOnSucceeded(stateEvent -> {
            installButton.setVisible(true);
            installHBox.setVisible(false);

            if (!checkInstalledAndSetButton()) {
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
}
