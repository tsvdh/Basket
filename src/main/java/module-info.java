module basket {
    requires basket.api;

    requires java.desktop;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    // requires logback.classic;
    // requires org.slf4j;
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