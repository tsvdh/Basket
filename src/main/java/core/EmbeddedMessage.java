package core;

import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;

public class EmbeddedMessage extends AnchorPane {

    public static EmbeddedMessage newEmbeddedMessage(String message) {
        EmbeddedMessage embeddedMessage = new EmbeddedMessage(message);
        embeddedMessage.retryButton.setVisible(false);
        return embeddedMessage;
    }

    public static EmbeddedMessage newEmbeddedErrorMessage(String message, EventHandler<ActionEvent> eventHandler) {
        EmbeddedMessage embeddedMessage = new EmbeddedMessage(message);
        embeddedMessage.retryButton.setOnAction(eventHandler);
        return embeddedMessage;
    }

    private EmbeddedMessage(String message) {
        super();

        URL url = getClass().getResource("/fxml/embedded_message.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (message.equals("loading")) {
            this.messageLabel.setVisible(false);
            this.loadingIndicator.setVisible(true);
        } else {
            this.messageLabel.setText(message);
        }
    }

    @FXML
    public Button retryButton;

    @FXML
    public Label messageLabel;

    @FXML
    public ProgressBar loadingIndicator;
}
