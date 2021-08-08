package core;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        //noinspection ResultOfMethodCallIgnored
        Basket.getInstance();
    }

    public static void main(String[] args) {
        launch();
    }
}
