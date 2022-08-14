package main;

import basket.api.app.BasketApp;
import basket.api.handlers.StyleHandler;
import basket.api.prebuilt.Message;
import core.Basket;
import java.io.IOException;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.Style;
import org.jetbrains.annotations.Nullable;
import server.ServerConnectionException;
import util.ThreadHandler;

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
                throwable.printStackTrace();
            }
        });

        new Thread(() -> {throw new RuntimeException();}).start();
        ThreadHandler.execute(() -> {throw new RuntimeException();});

        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                throw new IOException();
            }
        };

        task.setOnFailed(event -> System.out.println("test"));

        ThreadHandler.execute(task);

        // Platform.exit();
        MyApp.launch(MyApp.class);
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
