package update;

import common.StyleHandler;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static common.ButtonFactory.makeSmallButton;


class ProgressInfo {

    ProgressInfo(DownloadTask downloadTask, Updater updater, String newName) {
        String oldName = updater.getCurrentName();
        String newVersion = Updater.getVersionAsString(newName);
        Updater.Mode mode = updater.getMode();

        Label versionLabel = new Label();
        versionLabel.setFont(new Font(20));
        if (mode == Updater.Mode.INSTALLING) {
            versionLabel.setText("Installing version: " + newVersion);
        } else {
            String oldVersion = Updater.getVersionAsString(oldName);
            String begin;
            if (mode == Updater.Mode.UPDATING) {
                begin = "New version available: ";
            }
            else {
                begin = "Switching to: ";
            }
            versionLabel.setText(begin + newVersion + " (current: " + oldVersion + ")");
        }

        Label progressLabel = new Label();
        progressLabel.setFont(new Font(14));
        progressLabel.setVisible(false);
        progressLabel.textProperty().bind(downloadTask.messageProperty());

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.progressProperty().bind(downloadTask.progressProperty());

        VBox progressContainer = new VBox();
        progressContainer.getChildren().addAll(progressBar, progressLabel);
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setSpacing(20);
        progressContainer.setPadding(new Insets(0, 0, 20, 0));

        Button button = makeSmallButton("Start");

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(versionLabel, button);
        vBox.setSpacing(20);
        vBox.setPadding(new Insets(10));
        vBox.setMinWidth(400);
        vBox.setMinHeight(100);

        Scene scene = new Scene(vBox);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Progress info");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("images/icon.png"));


        Executor executor = Executors.newSingleThreadExecutor();

        PauseTransition pauseTransition = new PauseTransition();
        pauseTransition.setDuration(Duration.seconds(5));
        pauseTransition.setOnFinished(event -> progressLabel.setVisible(true));

        button.setOnAction(event -> {
            vBox.getChildren().remove(button);
            vBox.getChildren().add(progressContainer);

            executor.execute(downloadTask);

            pauseTransition.play();
        });

        downloadTask.setOnSucceeded(event -> {
            updater.setSuccess(true);
            stage.close();
        });

        downloadTask.setOnFailed(event -> {
            updater.setSuccess(false);
            stage.close();
        });

        StyleHandler.applyStyle(scene);

        stage.setOnCloseRequest(Event::consume);
        stage.showAndWait();
    }
}
