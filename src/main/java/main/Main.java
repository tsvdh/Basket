package main;

import basket.api.app.BasketApp;
import basket.api.handlers.StyleHandler;
import basket.api.prebuilt.Message;
import core.Basket;
import javafx.application.Application;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.Style;
import org.jetbrains.annotations.Nullable;
import server.ServerConnectionException;

public class Main extends Application {

    public static class MyApp extends BasketApp {

        @Override
        protected @Nullable Class<?> getSettingsObjectClass() {
            return Settings.class;
        }

        @Override
        protected void start() {
            // Init singleton
            //noinspection ResultOfMethodCallIgnored
            Basket.getInstance();
        }

        @Override
        protected StyleHandler makeStyleHandler() {
            Style jMetroStyle = getSettingsHandler().getConvertedObject(Settings.class).getJmetroStyle();
            return new StyleHandler(jMetroStyle);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof ServerConnectionException) {
                new Message(throwable.getMessage(), true);
            } else {
                System.err.printf("Exception in thread '%s': ", Thread.currentThread().getName());
                throwable.printStackTrace();
            }
        });

        MyApp.launch(MyApp.class);
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
