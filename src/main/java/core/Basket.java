package core;

import core.library.LibraryLoadTask;
import core.store.StoreLoadTask;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import server.ServerConnectionException;
import server.ServerHandler;

import static core.EmbeddedMessage.newEmbeddedLoadingMessage;
import static core.EmbeddedMessage.newEmbeddedMessage;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class Basket {

    private static final class InstanceHolder {
        private static final Basket INSTANCE = new Basket();
    }

    public static Basket getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public final BasketController controller;

    private Basket() {
        URL url = getClass().getResource("/fxml/basket.fxml");
        FXMLLoader loader = new FXMLLoader(url);

        Stage stage;
        try {
            stage = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        controller = loader.getController();
        controller.init();

        stage.show();

        loadStore();
        loadLibrary();
    }

    public void loadStore() {
        List<Node> items = controller.storeVBox.getChildren();
        items.clear();
        items.add(newEmbeddedMessage("Loading..."));

        newSingleThreadExecutor().execute(() -> {
            try {
                if (ServerHandler.serverSleeping()) {
                    Platform.runLater(() -> {
                        items.clear();
                        items.add(newEmbeddedLoadingMessage("Server is starting, please wait a moment"));
                    });
                }
            } catch (ServerConnectionException ignored) {
                // No need to display error here, as that is done by the store load task
            }
        });

        StoreLoadTask task = new StoreLoadTask();
        task.setOnSucceeded(event -> {
            items.clear();
            items.addAll(task.getValue());
        });

        newSingleThreadExecutor().execute(task);
    }

    public void loadLibrary() { // TODO: more seamless alternative
        List<Node> items = controller.libraryVBox.getChildren();
        items.clear();
        items.add(newEmbeddedMessage("Loading..."));

        LibraryLoadTask task = new LibraryLoadTask();
        task.setOnSucceeded(event -> {
            items.clear();
            items.addAll(task.getValue());
        });

        newSingleThreadExecutor().execute(task);
    }
}
