package common.popups;

import common.SettingsHandler;
import common.StyleHandler;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static common.ButtonFactory.makeSmallButton;


public class Confirmation {

    private String result;

    public Confirmation() {
        this("Do you want to perform the action?");
    }

    public Confirmation(String question) {
        result = "yes";

        boolean confirmations = SettingsHandler.getInstance().getConfirmations();
        if (!confirmations) {
            return;
        }

        Button yesButton = makeSmallButton("Yes");

        Button noButton = makeSmallButton("No");

        HBox hBox = new HBox();
        hBox.getChildren().addAll(yesButton,
                                noButton);
        hBox.setSpacing(50);
        hBox.setAlignment(Pos.CENTER);

        Label label = new Label();
        label.setText(question);
        label.setFont(new Font(20));

        VBox vBox = new VBox();
        vBox.getChildren().addAll(label,
                                hBox);
        vBox.setSpacing(30);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Confirmation");
        stage.getIcons().add(new Image("images/icon.png"));

        yesButton.setOnAction(event -> stage.close());

        noButton.setOnAction(event -> {
            result = "no";
            stage.close();
        });

        StyleHandler.applyStyle(scene);

        stage.setOnCloseRequest(Event :: consume);
        stage.showAndWait();
    }

    public String getResult() {
        return result;
    }
}
