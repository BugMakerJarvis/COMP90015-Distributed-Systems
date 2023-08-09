module edu.unimelb.jarvis.common {
    requires javafx.graphics;
    requires javafx.controls;
    requires com.google.gson;
    requires static lombok;
    requires java.desktop;
    requires javafx.swing;

    exports edu.unimelb.jarvis.websocket;
    exports edu.unimelb.jarvis.enums;
    exports edu.unimelb.jarvis.graph;
    exports edu.unimelb.jarvis.core;
    exports edu.unimelb.jarvis.util;
}