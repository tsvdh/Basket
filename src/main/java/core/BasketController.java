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
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import main.Settings;
import prebuilt.Info;
import prebuilt.Message;

public class BasketController {

    private Basket basket;

    public void init(Basket basket) {
        this.basket = basket;

        tabPane.getSelectionModel().select(0); // open store tab at startup
        tabPane.getStyleClass().add(JMetroStyleClass.UNDERLINE_TAB_PANE);

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
    public void refresh_store() {
        basket.loadStore();
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
}
