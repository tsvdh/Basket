package main;

import app.BasketApp;
import app.Property;
import common.ExternalPropertiesHandler;
import core.StringQueue;
import java.io.IOException;
import java.util.function.Function;
import jfxtras.styles.jmetro.Style;
import util.Version;

public enum Settings implements Property {

    jmetro_style(Style::valueOf),
    installed_apps(StringQueue::parse),
    current_version(Version::parse);

    private final Function<String, Object> parser;

    Settings(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Function<String, Object> getParser() {
        return parser;
    }

    public static void addApp(String appName) throws IOException {
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);
        strings.add(appName);
        settingsHandler.setProperty(Settings.installed_apps, strings).save();
    }

    public static void removeApp(String appName) throws IOException {
        ExternalPropertiesHandler settingsHandler = BasketApp.getSettingsHandler();
        StringQueue strings = (StringQueue) settingsHandler.getProperty(Settings.installed_apps);
        strings.remove(appName);
        settingsHandler.setProperty(Settings.installed_apps, strings).save();
    }
}
