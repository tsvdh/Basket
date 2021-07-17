package core;

import common.PathHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Basket {

    public Basket() {
        URL url = getClass().getResource("/fxml/basket.fxml");
        FXMLLoader loader = new FXMLLoader(url);

        Stage stage;
        try {
            stage = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BasketController basketController = loader.getController();
        // basketController.init();

        try {
            stage.getIcons().add(new Image(PathHandler.getIconPath()));
        } catch (Exception ignored) {}

        // if (styleHandler != null) {
        //     styleHandler.applyStyle(stage.getScene());
        // }

        stage.show();
    }
}
