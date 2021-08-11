package main;

import app.BasketApp;
import common.pre_built.StyleHandler;
import core.Basket;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static class MyApp extends BasketApp {

        @Override
        public void start() {
            //noinspection ResultOfMethodCallIgnored
            Basket.getInstance();
        }

        @Override
        public StyleHandler makeStyleHandler() {
            return new StyleHandler("clean", StyleHandler.Location.EXTERNAL);
        }

        public static void invokeLaunch() {
            MyApp.launch();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        MyApp.invokeLaunch();
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
