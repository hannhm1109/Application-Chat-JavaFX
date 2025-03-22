module com.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.chatapp.client to javafx.fxml;
    opens com.chatapp.server to javafx.fxml;

    exports com.chatapp.client;
    exports com.chatapp.server;
    exports com.chatapp.model;
}