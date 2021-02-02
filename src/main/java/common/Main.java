package common;

import api.PropertiesHandler;
import common.user.Settings;

public class Main {

    public static void main(String[] args) throws Exception {
        PropertiesHandler propertiesHandler = PropertiesHandler.getInstance();

        Integer age = (Integer) propertiesHandler.getSetting(Settings.age);
        System.out.println(age);
        System.out.println(age + 1);

        propertiesHandler.setSetting(Settings.age, age);
    }
}
