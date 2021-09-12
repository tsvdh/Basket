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
import org.jetbrains.annotations.Nullable;

public class EmbeddedMessage extends AnchorPane {

    public static EmbeddedMessage newEmbeddedMessage(String message) {
        EmbeddedMessage embeddedMessage = new EmbeddedMessage();

        embeddedMessage.messageLabel.setVisible(true);
        embeddedMessage.messageLabel.setText(message);

        return embeddedMessage;
    }

    public static EmbeddedMessage newEmbeddedErrorMessage(String message, EventHandler<ActionEvent> eventHandler) {
        EmbeddedMessage embeddedMessage = newEmbeddedMessage(message);

        embeddedMessage.retryButton.setVisible(true);
        embeddedMessage.retryButton.setOnAction(eventHandler);

        return embeddedMessage;
    }

    public static EmbeddedMessage newEmbeddedLoadingMessage(@Nullable String loadingMessage) {
        EmbeddedMessage embeddedMessage = new EmbeddedMessage();

        embeddedMessage.loadingIndicator.setVisible(true);

        if (loadingMessage != null) {
            embeddedMessage.loadingMessageLabel.setVisible(true);
            embeddedMessage.loadingMessageLabel.setText(loadingMessage);
        }

        return embeddedMessage;
    }

    public EmbeddedMessage() {
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
    }

    @FXML
    public Button retryButton;

    @FXML
    public Label messageLabel;

    @FXML
    public ProgressBar loadingIndicator;

    @FXML
    public Label loadingMessageLabel;
}
