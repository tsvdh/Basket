package core;

import core.store.StoreLoadTask;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;

import static core.store.StoreMessage.newStoreMessage;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class Basket {

    private static final class InstanceHolder {
        private static final Basket INSTANCE = new Basket();
    }

    public static Basket getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final BasketController basketController;

    private Basket() {
        URL url = getClass().getResource("/fxml/basket.fxml");
        FXMLLoader loader = new FXMLLoader(url);

        Stage stage;
        try {
            stage = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        basketController = loader.getController();
        basketController.init(this);

        stage.show();

        loadStore();
    }

    public void loadStore() {
        List<Node> items = basketController.storeVBox.getChildren();
        items.clear();
        items.add(newStoreMessage("Loading..."));

        StoreLoadTask task = new StoreLoadTask();
        task.setOnSucceeded(event -> {
            items.clear();
            items.addAll(task.getValue());
        });

        newSingleThreadExecutor().execute(task);
    }
}
