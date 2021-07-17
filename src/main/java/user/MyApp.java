package user;

import app.App;
import common.ExternalPropertiesHandler;

public class MyApp extends App {

    @Override
    public void start() {
        System.out.println("Hello world!");
    }

    public static void main(String[] args) {
        run();
    }
}
