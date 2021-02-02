package api;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PropertiesHandler {

    private static PropertiesHandler instance;

    public static PropertiesHandler getInstance() {
        if (instance == null) {
            synchronized (PropertiesHandler.class) {
                if (instance == null) {
                    instance = new PropertiesHandler();
                }
            }
        }
        return instance;
    }

    private final Properties pomProperties;
    private Properties settings;

    private PropertiesHandler() {
        pomProperties = FileHandler.getPomProperties();

        Properties defaultSettings = FileHandler.getDefaultSettings();
        settings = FileHandler.readSettings();
        if (settings == null) {
            // file exists, but is not readable
            // TODO add javafx warning
            settings = defaultSettings;
        }
        else {
            Set<String> defaultKeys = defaultSettings.stringPropertyNames();
            Set<String> settingsKeys = settings.stringPropertyNames();

            Set<String> missingKeys = new HashSet<>(defaultKeys);
            missingKeys.removeAll(settingsKeys);

            for (String missing : missingKeys) {
                settings.setProperty(missing, defaultSettings.getProperty(missing));
            }
        }
    }

    public void storeSettings() {
        boolean success = FileHandler.writeSettings(settings);
        if (!success) {
            // TODO add javafx warning
        }
    }

    public String getAppName() {
        return pomProperties.getProperty("name");
    }

    public Version getAppVersion() {
        return new Version(pomProperties.getProperty("version"));
    }

    public Object getSetting(Property property) {
        String value = settings.getProperty(property.toString());
        return property.getParser().apply(value);
    }

    public void setSetting(Property property, Object value) {
        settings.setProperty(property.toString(), String.valueOf(value));
    }
}
