package core.store;

import core.Basket;
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
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.FileName;
import server.common.model.app.App;

import static util.ThreadHandler.execute;

public class StoreItem extends AnchorPane {

    private final App app;

    public StoreItem(App app) {
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
        if (!app.isAvailable()) {
            return;
        }

        addButton.setVisible(false);
        progressBar.setVisible(true);

        var task = new AcquireTask(app);

        task.setOnSucceeded(event -> {

            addButton.setVisible(true);
            progressBar.setVisible(false);

            if (task.getValue()) {

                // update library
                addButton.setDisable(true);
                addButton.setText("In library");

                Basket.getInstance().loadLibrary();
            }
        });

        execute(task);
    }
}
