package core.library;

import core.Basket;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.scene.Node;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.model.app.App;

import static java.util.Comparator.comparing;

public class LibraryLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        Set<String> localLibraryAppIds = Basket.getInstance().getUserInfo().getUsageInfo().keySet();

        List<App> serverLibraryApps = null;
        try {
            serverLibraryApps = ServerHandler.getInstance().getLibraryApps();
        } catch (ServerConnectionException ignored) {}

        if (serverLibraryApps != null) {
            return serverLibraryApps.stream()
                    .map(LibraryItem::new)
                    .sorted(comparing(libraryItem -> libraryItem.getApp().getName()))
                    .collect(Collectors.toList());
        } else {
            return localLibraryAppIds.stream()
                    .map(appId -> {
                        try {
                            return new LibraryItem(appId);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(comparing(libraryItem -> libraryItem.getApp().getName()))
                    .collect(Collectors.toList());
        }
    }
}
