package core;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class StoreError extends AnchorPane {

    private final Basket basket;

    public StoreError(Basket basket) {
        super();

        URL url = getClass().getResource("/fxml/store_error.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.basket = basket;
    }

    @FXML
    public void retryLoading() {
        basket.loadStore();
    }
}
