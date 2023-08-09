module edu.unimelb.jarvis.join {
    requires javafx.controls;
    requires javafx.fxml;
    requires edu.unimelb.jarvis.common;
    requires com.google.gson;
    requires javax.websocket.api;
    requires tyrus.client;

    exports edu.unimelb.jarvis.join;
    exports edu.unimelb.jarvis.join.ui;
    exports edu.unimelb.jarvis.join.websocket;
}