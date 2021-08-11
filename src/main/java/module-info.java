module basket {
    requires basket.api;

    requires java.desktop;
    requires mongo.java.driver;
    requires logback.classic;
    requires org.slf4j;

    exports core;
    exports main;

    opens properties to basket.api;
    opens images to basket.api;
}