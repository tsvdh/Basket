package core.store;

import core.Basket;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import server.ServerConnectionException;
import server.ServerHandler;
import server.common.model.app.App;

import static core.EmbeddedMessage.newEmbeddedErrorMessage;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;

public class StoreLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        List<App> apps;
        try {
            apps = ServerHandler.getInstance().getStoreApps();
        } catch (ServerConnectionException e) {
            return singletonList(newEmbeddedErrorMessage("Could not connect to the server",
                    event -> Basket.getInstance().loadStore()));
        }

        List<Node> items = new LinkedList<>();

        apps.sort(comparing(App::getName));

        apps.forEach(app -> items.add(new StoreItem(app)));

        return items;
    }
}
