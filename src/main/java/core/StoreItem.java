package core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.bson.Document;

import static java.util.Objects.requireNonNull;

public class StoreItem extends AnchorPane {

    private boolean valid;

    public StoreItem(Document document) {
        super();

        URL url = getClass().getResource("/fxml/store_item.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.valid = true;

        try {
            name.setText(requireNonNull(document.getString("name")));
            description.setText(requireNonNull(document.getString("description")));

            String address = requireNonNull(document.getString("icon_address"));
            InputStream inputStream = new URL(address).openStream();
            if (inputStream != null) {
                icon.setImage(new Image(inputStream));
            }
        } // these exceptions indicate a bad entry in the database, so they won't be added to the store
        catch (ClassCastException | NullPointerException | MalformedURLException e) { // TODO: add some kind of alert for dev/admin
            this.valid = false;
        } catch (IOException ignored) {}
    }

    public boolean isValid() {
        return valid;
    }

    @FXML
    public Label name;

    @FXML
    public Label description;

    @FXML
    public Button installButton;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public ImageView icon;

    @FXML
    public void install(ActionEvent event) {

    }
}
