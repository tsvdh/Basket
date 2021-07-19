package core;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class StoreItem extends AnchorPane {

    public StoreItem() {
        super();

        URL url = getClass().getResource("/fxml/store_item.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public Label description;

    @FXML
    public Button installButton;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public void install(ActionEvent event) {

    }
}
