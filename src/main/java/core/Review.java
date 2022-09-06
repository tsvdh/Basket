package core;

import basket.api.prebuilt.Message;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.model.app.App;

public class Review {

    public Review(App app) {
        URL url = getClass().getResource("/fxml/review.fxml");
        FXMLLoader loader = new FXMLLoader(url);

        Stage stage;
        try {
            stage = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ReviewController controller = loader.getController();
        controller.init(stage, app.getName());

        stage.show();

        stage.setOnHiding(event -> {
            Integer grade = controller.getGrade();
            if (grade == null) {
                return;
            }

            try {
                ServerHandler.getInstance().uploadReview(app.getId(), grade);
            } catch (ServerConnectionException e) {
                new Message("Failed to upload review: " + e.getMessage(), true);
            }
            try {
                Basket.getInstance().refreshUser();
            } catch (ServerConnectionException e) {
                System.err.println("Could not refresh user");
            }
        });
    }
}
