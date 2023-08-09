module edu.unimelb.jarvis.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires edu.unimelb.jarvis.common;
    requires static lombok;
    requires javax.websocket.api;
    requires tyrus.server;
    requires com.google.gson;

    exports edu.unimelb.jarvis.server;
    exports edu.unimelb.jarvis.server.ui;
    exports edu.unimelb.jarvis.server.websocket;
}