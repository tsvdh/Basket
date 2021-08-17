package core.store;

import core.Basket;
import db.DBConnectException;
import db.DBConnection;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.bson.Document;

import static core.EmbeddedMessage.newEmbeddedErrorMessage;
import static java.util.Collections.singletonList;

public class StoreLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        Iterable<Document> documents;
        try {
            documents = DBConnection.getInstance().getAppInfo();
        } catch (DBConnectException e) {
            return singletonList(newEmbeddedErrorMessage(e.getMessage(),
                    event -> Basket.getInstance().loadStore()));
        }

        LinkedList<Node> items = new LinkedList<>();

        for (Document document : documents) {
            StoreItem storeItem = new StoreItem(document);

            if (storeItem.isValid()) {
                items.add(storeItem);
            }
        }

        // TODO: sort items

        return items;
    }
}
