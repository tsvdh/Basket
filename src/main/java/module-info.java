module basket {
    requires basket.api;

    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires lombok;
    requires org.jfxtras.styles.jmetro;
    requires zip4j;
    requires org.apache.commons.io;
    requires java.net.http;
    requires javafx.fxml;
    requires org.jetbrains.annotations;
    requires libphonenumber;

    opens data to basket.api;
    opens images to basket.api;

    exports core;
    exports core.library;
    exports core.store;
    exports main;
    exports server;
    exports util;
    exports server.common;
    exports server.common.model.app;
    exports server.common.model.user;
}