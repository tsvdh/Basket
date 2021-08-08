package core;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class StoreMessage extends AnchorPane {

    public static StoreMessage newStoreMessage(String message) {
        StoreMessage storeMessage = new StoreMessage(message);
        storeMessage.retryButton.setVisible(false);
        return storeMessage;
    }

    public static StoreMessage newStoreErrorMessage(String message) {
        StoreMessage storeMessage = new StoreMessage(message);
        storeMessage.retryButton.setOnAction(event -> Basket.getInstance().loadStore());
        return storeMessage;
    }

    private StoreMessage(String message) {
        super();

        URL url = getClass().getResource("/fxml/store_message.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.message.setText(message);
    }

    @FXML
    public Button retryButton;

    @FXML
    public Label message;
}
