package db;

import java.net.URL;
import org.bson.Document;
import util.Version;
import util.url.BadURLException;

import static java.util.Objects.requireNonNull;
import static util.url.URLConstructor.makeURL;

// Every method can throw a runtime exception
public record DocumentHelper(Document document) {

    public String getName() {
        return requireNonNull(document.getString("name"));
    }

    public String getDescription() {
        return requireNonNull(document.getString("description"));
    }

    public URL getIconURL() {
        String iconAddress = requireNonNull(document.getString("icon_address"));
        if (!iconAddress.endsWith(".png")) {
            throw new BadURLException();
        }
        return makeURL(iconAddress);
    }

    public URL getGithubHome() {
        return makeURL(requireNonNull(document.getString("github_home")));
    }

    private Document getVersionDocument() {
        return (Document) requireNonNull(document.get("versions"));
    }

    public Version getStableVersion() {
        return Version.of(requireNonNull(getVersionDocument().getString("stable")));
    }
    public Version getExperimentalVersion() {
        return Version.of(requireNonNull(getVersionDocument().getString("experimental")));
    }
}
