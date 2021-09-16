package core.store;

import core.App;
import core.Basket;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import server.ServerConnectionException;
import server.ServerHandler;

import static core.EmbeddedMessage.newEmbeddedErrorMessage;
import static java.util.Collections.singletonList;

public class StoreLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        List<App> apps;
        try {
            apps = ServerHandler.getStoreApps();
        } catch (ServerConnectionException e) {
            return singletonList(newEmbeddedErrorMessage("Could not connect to the server",
                    event -> Basket.getInstance().loadStore()));
        }

        LinkedList<Node> items = new LinkedList<>();

        apps.forEach(app -> items.add(new StoreItem(app)));

        // TODO: sort items

        return items;
    }
}
