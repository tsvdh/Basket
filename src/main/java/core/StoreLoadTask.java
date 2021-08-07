package core;

import db.DBConnectException;
import db.DBConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.bson.Document;

class StoreLoadTask extends Task<List<Node>> {

    private final StoreError storeError;

    StoreLoadTask(Basket basket) {
        storeError = new StoreError(basket);
    }

    @Override
    protected List<Node> call() {
        return getStoreItems();
    }

    private List<Node> getStoreItems() {
        Iterable<Document> documents;
        try {
            documents = DBConnection.getInstance().getAppInfo();
        } catch (DBConnectException e) {
            return Collections.singletonList(storeError);
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
