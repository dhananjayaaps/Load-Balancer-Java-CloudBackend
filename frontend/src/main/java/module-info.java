module com.cloudbackend.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires java.desktop;
    requires org.apache.httpcomponents.httpcore;
    requires spring.web;
    requires spring.core;
    requires static lombok;

    // Open package to both JavaFX and Jackson
    opens com.cloudbackend.frontend to javafx.fxml, com.fasterxml.jackson.databind;

    // Export the package
    exports com.cloudbackend.frontend;
}
