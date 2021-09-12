package core.library;

import basket.api.app.BasketApp;
import basket.api.common.ExternalPropertiesHandler;
import core.App;
import core.StringQueue;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Node;
import main.Settings;
import server.ServerConnectionException;
import server.ServerHandler;

import static core.EmbeddedMessage.newEmbeddedMessage;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;

public class LibraryLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue installedNames = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);

        if (installedNames.isEmpty()) {
            return singletonList(newEmbeddedMessage("No apps installed"));
        }

        List<App> installedApps;
        try {
            installedApps = ServerHandler.getLibraryApps(installedNames);
            installedApps.sort(comparing(App::getName));
        } catch (ServerConnectionException e) {
            installedApps = null;
        }

        List<Node> items = new LinkedList<>();

        for (String name : installedNames) {
            App app;
            if (installedApps == null) {
                app = null;
            } else {
                app = installedApps.remove(0);
            }

            items.add(new LibraryItem(name, app));
        }

        return items;
    }
}
