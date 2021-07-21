package core;

import common.pre_built.popups.Message;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class BasketController {

    public void init() {

    }

    @FXML
    public VBox storeVBox;

    @FXML
    public VBox libraryVBox;

    @FXML
    public TabPane tabPane;

    @FXML
    public void openReadme() throws URISyntaxException {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/tsvdh/Basket#readme"));
        } catch (IOException e) {
            new Message("Could not open the website", true, null);
        }
    }

}
