package main;

import basket.api.app.BasketApp;
import basket.api.common.StyleHandler;
import core.Basket;
import javafx.application.Application;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {

    public static class MyApp extends BasketApp {

        @Override
        public void start() {
            //noinspection ResultOfMethodCallIgnored
            Basket.getInstance();
            // try {
            //     System.out.println(Class.forName("org.slf4j.Logger"));
            // } catch (ClassNotFoundException e) {
            //     e.printStackTrace();
            // }
            // System.exit(0);
        }

        @Override
        public StyleHandler makeStyleHandler() {
            Style jMetroStyle = (Style) BasketApp.getSettingsHandler().getProperty(Settings.jmetro_style);
            return new StyleHandler(jMetroStyle);
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
