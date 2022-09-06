package core;

import basket.api.prebuilt.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;

public class ReviewController {

    private Stage stage;
    private Integer grade;

    public void init(Stage stage, String appName) {
        this.stage = stage;
        this.grade = null;

        label.setText("Review: " + appName);
    }

    public Integer getGrade() {
        return grade;
    }

    @FXML
    public Label label;

    @FXML
    public Rating rating;

    @FXML
    public void submit() {
        if ((int) rating.getRating() < 1) {
            new Message("You must give at least one star to this app", false);
            return;
        }

        this.grade = (int) rating.getRating();
        stage.close();
    }
}
