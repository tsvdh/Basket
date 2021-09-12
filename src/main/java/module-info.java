module basket {
    requires basket.api;

    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires lombok;
    requires org.jfxtras.styles.jmetro;
    requires zip4j;
    requires org.apache.commons.io;
    requires java.net.http;
    requires javafx.fxml;
    requires org.jetbrains.annotations;

    opens properties to basket.api;
    opens images to basket.api;

    exports core;
    exports core.library;
    exports core.store;
    exports main;
    exports server;
}