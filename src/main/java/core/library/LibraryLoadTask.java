package core.library;

import app.BasketApp;
import com.mongodb.lang.Nullable;
import common.ExternalPropertiesHandler;
import core.StringQueue;
import db.DBConnectException;
import db.DBConnection;
import db.DocumentHelper;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import main.Settings;
import org.bson.Document;

import static core.EmbeddedMessage.newEmbeddedMessage;
import static java.util.Collections.singletonList;

public class LibraryLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue installed = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);

        if (installed.isEmpty()) {
            return singletonList(newEmbeddedMessage("No apps installed"));
        }

        Iterable<Document> documents;
        try {
            documents = DBConnection.getInstance().getAppInfo();
        } catch (DBConnectException e) {
            documents = null;
        }

        List<Node> items = new LinkedList<>();

        for (String appName : installed) {
            Document matchingDocument = getDocument(documents, appName);
            items.add(new LibraryItem(appName, matchingDocument));
        }

        return items;
    }

    private @Nullable Document getDocument(Iterable<Document> documents, String name) {
        if (documents == null) {
            return null;
        }

        for (Document document : documents) {
            try {
                String documentName = new DocumentHelper(document).getName();
                if (documentName.equals(name)) {
                    return document;
                }
            } catch (RuntimeException e) {
                // ignore bad DB entry
            }
        }
        return null;
    }
}
