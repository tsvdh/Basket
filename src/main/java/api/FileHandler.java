package api;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

class FileHandler {

    static final String launcherName = "Basket";

    private static String getInternalPropertiesPath(String fileName) {
        return "properties/" + fileName + ".properties";
    }

    private static final String pomPropertiesPath = getInternalPropertiesPath("pom");
    private static final String defaultSettingsPath = getInternalPropertiesPath("settings");

    static Properties getPomProperties() {
        Properties pomProperties = new Properties();
        try {
            pomProperties.load(ClassLoader.getSystemResourceAsStream(pomPropertiesPath));
        } catch (Exception e) {
            System.err.println("Fatal: could not get application information: " + e.getMessage());
            System.exit(1);
        }
        return pomProperties;
    }

    static Properties getDefaultSettings() {
        Properties defaultSettings = new Properties();
        try {
            defaultSettings.load(ClassLoader.getSystemResourceAsStream(defaultSettingsPath));
        } catch (Exception e) {
            System.err.println("Fatal: could not get default settings " + e.getMessage());
            System.exit(1);
        }
        return defaultSettings;
    }

    // folder and file exist and app has access to file, as it is ensured by the api
    static @Nullable Properties readSettings() {
        File file = new File(PathHandler.getSettingsFilePath());
        Properties settings = new Properties();
        try {
            settings.load(new FileReader(file));
        } catch (IOException e) {
            System.err.println("Could not read settings: " + e.getMessage());
            settings = null;
        }
        return settings;
    }

    static boolean writeSettings(Properties settings) {
        File file = new File(PathHandler.getSettingsFilePath());
        try {
            settings.store(new FileWriter(file), null);
        } catch (IOException e) {
            System.err.println("Could not write settings: " + e.getMessage());
            return false;
        }
        return true;
    }
}
