module basket {
    requires basket.api;

    requires java.desktop;
    requires mongo.java.driver;
    // requires org.apache.logging.log4j;
    requires logback.classic;
    requires org.slf4j;

    exports core;
}