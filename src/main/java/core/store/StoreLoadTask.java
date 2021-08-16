package core.store;

import db.DBConnectException;
import db.DBConnection;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.bson.Document;

import static core.store.StoreMessage.newStoreErrorMessage;
import static java.util.Collections.singletonList;

public class StoreLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        return getStoreItems();
    }

    private List<Node> getStoreItems() {
        Iterable<Document> documents;
        try {
            documents = DBConnection.getInstance().getAppInfo();
        } catch (DBConnectException e) {
            return singletonList(newStoreErrorMessage(e.getMessage()));
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
