module edu.unimelb.jarvis.dictionaryclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires org.slf4j;
    requires logback.classic;
    requires javafx.swing;
    requires javafx.media;
    requires io.vproxy.vfx;

    exports edu.unimelb.jarvis;
    exports edu.unimelb.jarvis.javafx;
    exports edu.unimelb.jarvis.dict;
}