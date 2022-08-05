package core.library;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.scene.Node;
import main.Settings;

import static basket.api.app.BasketApp.getSettingsHandler;

public class LibraryLoadTask extends Task<List<Node>> {

    @Override
    protected List<Node> call() {
        Set<String> acquiredApps = getSettingsHandler().getConvertedObject(Settings.class).getAcquiredApps();

        return acquiredApps.stream()
                .sorted(Comparator.naturalOrder())
                .map(LibraryItem::new)
                .collect(Collectors.toList());
    }
}
