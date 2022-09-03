package core.store;

import basket.api.prebuilt.Message;
import core.Basket;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import server.ServerConnectionException;
import server.ServerHandler;
import server.ServerHandler.LibraryAction;
import server.common.FileName;
import server.common.model.app.App;
import server.common.model.user.User;

import static util.ThreadHandler.execute;

public class StoreItem extends AnchorPane {

    private final App app;

    public StoreItem(App app, User user) {
        super();

        URL fxmlUrl = getClass().getResource("/fxml/store_item.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.app = app;

        nameLabel.setText(app.getName());
        descriptionLabel.setText(app.getDescription());

        Float grade = app.getAppStats().getRating().getGrade();
        ratingLabel.setText("%s (%s)".formatted(grade != null ? grade : "-",
                                                app.getAppStats().getRating().getReviews().keySet().size()));

        execute(() -> {
            try {
                InputStream iconStream = ServerHandler.getInstance()
                        .getDownloadStream(app.getId(), FileName.ICON).body();

                icon.setImage(new Image(iconStream));
            }
            catch (ServerConnectionException e) {
                System.err.println(e.getMessage());
            }
        });

        if (!app.isAvailable()) {
            addButton.setDisable(true);
            addButton.setText("Not available yet");
        } else if (user.getUsageInfo().containsKey(app.getId())) {
            addButton.setDisable(true);
            addButton.setText("In library");
        }
    }

    @FXML
    public Label nameLabel;

    @FXML
    public Label descriptionLabel;

    @FXML
    public ImageView icon;

    @FXML
    public Label ratingLabel;

    @FXML
    public Button addButton;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public void addToLibrary() {
        // will only be executed if app is available and not in library yet

        addButton.setVisible(false);
        progressBar.setVisible(true);

        // try to notify server
        try {
            ServerHandler.getInstance().modifyLibrary(app.getId(), LibraryAction.add);
        } catch (ServerConnectionException e) {
            Platform.runLater(() -> new Message("Could not connect to server", true));
            return;
        } finally {
            addButton.setVisible(true);
            progressBar.setVisible(false);
        }

        addButton.setDisable(true);
        addButton.setText("In library");

        try {
            Basket.getInstance().refreshUser();
        } catch (ServerConnectionException e) {
            Platform.runLater(() -> new Message("Could not connect to server,"
                    + "\nplease refresh library with internet access", true));
        }

        Basket.getInstance().loadLibrary();
    }
}
