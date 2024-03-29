package core;

import basket.api.app.BasketApp;
import basket.api.prebuilt.Info;
import basket.api.prebuilt.Message;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.Style;
import main.Settings;

import static basket.api.app.BasketApp.getSettingsHandler;

public class BasketController {

    public void init() {
        tabPane.getSelectionModel().select(0); // open store tab at startup

        Style jMetroStyle = getSettingsHandler()
                .getConvertedObject(Settings.class).getJmetroStyle();
        if (jMetroStyle == Style.DARK) {
            darkModeMenuItem.setSelected(true);
        }
    }

    @FXML
    public VBox storeVBox;

    @FXML
    public VBox libraryVBox;

    @FXML
    public TabPane tabPane;

    @FXML
    public void openReadme() throws URISyntaxException {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/tsvdh/Basket#readme"));
        } catch (IOException e) {
            new Message("Could not open the website", true);
        }
    }

    @FXML
    public void showAbout() {
        List<String> infoList = new LinkedList<>();
        infoList.add("Made by Tobias van den Hurk");
        infoList.add("-");
        //noinspection SpellCheckingInspection
        infoList.add("Connected to Atlas MongoDB with a RESTful Spring Boot API");
        new Info(infoList);
    }

    @FXML
    public CheckMenuItem darkModeMenuItem;

    @FXML
    public void swapJMetroTheme() {
        var settings = getSettingsHandler().getConvertedObject(Settings.class);
        Style jMetroStyle = settings.getJmetroStyle();

        if (jMetroStyle == Style.LIGHT) {
            jMetroStyle = Style.DARK;
        } else {
            jMetroStyle = Style.LIGHT;
        }

        BasketApp.getStyleHandler().reStyleJMetro(jMetroStyle);

        settings.setJmetroStyle(jMetroStyle);
        try {
            getSettingsHandler().save();
        } catch (IOException e) {
            new Message("Could not save settings", true);
        }
    }

    @FXML
    public MenuItem refreshStoreButton;

    @FXML
    public void refreshStore() {
        Basket.getInstance().loadStore();
    }

    @FXML
    public MenuItem refreshLibraryButton;

    @FXML
    public void refreshLibrary() {
        Basket.getInstance().loadLibrary();
    }
}
