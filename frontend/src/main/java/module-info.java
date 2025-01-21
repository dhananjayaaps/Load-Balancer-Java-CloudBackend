module com.cloudbackend.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens com.cloudbackend.frontend to javafx.fxml;
    exports com.cloudbackend.frontend;
}
