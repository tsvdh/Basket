package core;

import app.BasketApp;
import common.PropertiesHandler;
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
import prebuilt.Info;
import prebuilt.Message;

public class BasketController {

    public void init() {
        tabPane.getSelectionModel().select(0); // open store tab at startup

        PropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        Style jMetroStyle = (Style) settingsHandler.getProperty(Settings.jmetro_style);
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
        new Info(infoList);
    }

    @FXML
    public CheckMenuItem darkModeMenuItem;

    @FXML
    public void swapJMetroTheme() {
        PropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        Style jMetroStyle = (Style) settingsHandler.getProperty(Settings.jmetro_style);

        if (jMetroStyle == Style.LIGHT) {
            jMetroStyle = Style.DARK;
        } else {
            jMetroStyle = Style.LIGHT;
        }

        settingsHandler.setProperty(Settings.jmetro_style, jMetroStyle);
        BasketApp.getStyleHandler().reStyleJMetro(jMetroStyle);
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
