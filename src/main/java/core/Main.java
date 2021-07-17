package core;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new Basket();
    }

    public static void main(String[] args) {
        launch();
    }
}
