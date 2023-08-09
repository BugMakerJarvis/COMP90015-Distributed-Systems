module edu.unimelb.jarvis.create {
    requires javafx.controls;
    requires javafx.fxml;
    requires edu.unimelb.jarvis.common;
    requires com.google.gson;
    requires javax.websocket.api;
    requires tyrus.client;

    exports edu.unimelb.jarvis.create;
    exports edu.unimelb.jarvis.create.ui;
    exports edu.unimelb.jarvis.create.websocket;
}