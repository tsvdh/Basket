package core;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class LibraryItem extends AnchorPane {

    public LibraryItem() {
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
    }

    @FXML
    public Label versionLabel;

    @FXML
    public Button launchButton;

    @FXML
    public Button removeButton;

    @FXML
    public Button moreButton;

    @FXML
    public Button updateButton;

    @FXML
    public void launch(ActionEvent event) {

    }

    @FXML
    public void more(ActionEvent event) {

    }

    @FXML
    public void remove(ActionEvent event) {

    }

    @FXML
    public void update(ActionEvent event) {

    }

}
