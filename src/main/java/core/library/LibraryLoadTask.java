package core.library;

import app.BasketApp;
import common.ExternalPropertiesHandler;
import core.StringQueue;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import main.Settings;

import static core.EmbeddedMessage.newEmbeddedMessage;
import static java.util.Collections.singletonList;

public class LibraryLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);

        if (strings.isEmpty()) {
            return singletonList(newEmbeddedMessage("No apps installed"));
        }

        List<Node> items = new LinkedList<>();

        for (String appName : strings) {
            items.add(new LibraryItem(appName));
        }

        return items;
    }
}
