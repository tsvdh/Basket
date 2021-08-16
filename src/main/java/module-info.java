module basket {
    requires basket.api;

    requires java.desktop;
    requires mongo.java.driver;
    requires logback.classic;
    requires org.slf4j;
    requires org.jfxtras.styles.jmetro;
    requires zip4j;
    requires org.apache.commons.io;

    opens properties to basket.api;
    opens images to basket.api;

    exports core.store;
    exports core.library;
    exports core;
    exports main;
}