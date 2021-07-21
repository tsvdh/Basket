module basket {
    requires basket.api;

    requires dropbox.core.sdk;
    requires java.desktop;

    // requires javafx.controls;
    // requires javafx.fxml;

    exports core;
}