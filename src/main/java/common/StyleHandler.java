package common;

import common.options.UISkin;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StyleHandler {

    private static final String STYLESHEET_LOCATION = "/style/";
    private static final String CLASSIC_NAME = "stylesheet_classic.css";
    private static final String CLEAN_NAME = "stylesheet_clean.css";

    public static void applyStyle(Scene scene) {
        SettingsHandler settingsHandler = SettingsHandler.getInstance();

        String name;
        if (settingsHandler.getUISkin() == UISkin.CLASSIC) {
            name = CLASSIC_NAME;
        } else {
            name = CLEAN_NAME;
        }

        String path = StyleHandler.class.getResource(STYLESHEET_LOCATION + name).toExternalForm();
        Parent root = scene.getRoot();

        root.setId("background");
        root.getStylesheets().add(path);
    }
}
