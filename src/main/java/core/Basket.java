package core;

import basket.api.handlers.JSONHandler;
import basket.api.handlers.PathHandler;
import basket.api.util.FatalError;
import core.library.LibraryLoadTask;
import core.store.StoreLoadTask;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import lombok.Getter;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.model.user.User;

import static core.EmbeddedMessage.newEmbeddedLoadingMessage;
import static core.EmbeddedMessage.newEmbeddedMessage;
import static util.ThreadHandler.execute;

public class Basket {

    private static final class InstanceHolder {
        private static final Basket INSTANCE = new Basket();
    }

    public static Basket getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public final BasketController controller;
    private JSONHandler<User> userInfoHandler;

    @Getter
    private User userInfo;

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

        execute(() -> {
            try {
                ServerHandler.getInstance().login("userA", "a12341234");
                userInfo = ServerHandler.getInstance().getUserInfo();
            } catch (ServerConnectionException ignored) {}

            Path userInfoPath = PathHandler.getExternalFilePath("user.json");

            if (userInfo == null && Files.exists(userInfoPath)) {
                try {
                    userInfoHandler = JSONHandler.read(userInfoPath, User.class);
                    userInfo = userInfoHandler.getObject();
                } catch (IOException ignored) {}
            }
            else if (userInfo != null) {
                try {
                    userInfoHandler = JSONHandler.create(userInfoPath, userInfo);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }

            Platform.runLater(() -> {
                if (userInfo == null) {
                    throw new FatalError("Could not get user info");
                } else {
                    loadStore();
                    loadLibrary();
                }
            });
        });
    }

    public User refreshUser() throws ServerConnectionException {
        // userInfo is not null

        userInfo = ServerHandler.getInstance().getUserInfo();

        try {
            userInfoHandler.save(userInfo);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return userInfo;
    }

    public void loadStore() {
        List<Node> items = controller.storeVBox.getChildren();
        items.clear();
        items.add(newEmbeddedMessage("Loading..."));

        checkAndShowServerSleeping(items);

        StoreLoadTask task = new StoreLoadTask();
        task.setOnSucceeded(event -> {
            items.clear();
            items.addAll(task.getValue());
        });

        execute(task);
    }

    public void loadLibrary() {
        List<Node> items = controller.libraryVBox.getChildren();
        items.clear();
        items.add(newEmbeddedMessage("Loading..."));

        Set<String> libraryAppIds = userInfo.getUsageInfo().keySet();

        if (!libraryAppIds.isEmpty()) {
            checkAndShowServerSleeping(items);
        }

        LibraryLoadTask task = new LibraryLoadTask();
        task.setOnSucceeded(event -> {
            items.clear();
            items.addAll(task.getValue());
        });

        execute(task);
    }

    private void checkAndShowServerSleeping(List<Node> items) {
        execute(() -> {
            try {
                if (ServerHandler.getInstance().serverSleeping()) {
                    Platform.runLater(() -> {
                        items.clear();
                        items.add(newEmbeddedLoadingMessage("Server is starting, please wait a moment"));
                    });
                }
            } catch (ServerConnectionException ignored) {
                // No need to display error here, as that is handled by final load result
            }
        });
    }
}
