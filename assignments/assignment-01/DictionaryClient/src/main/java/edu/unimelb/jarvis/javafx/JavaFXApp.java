package edu.unimelb.jarvis.javafx;

import edu.unimelb.jarvis.client.TCPInteractiveClient;
import io.vproxy.vfx.control.globalscreen.GlobalScreenUtils;
import io.vproxy.vfx.manager.task.TaskManager;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.util.FXUtils;
import io.vproxy.vfx.util.Logger;
import javafx.application.Application;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaFXApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        VStage stage = new VStage(primaryStage) {
            @Override
            public void close() {
                super.close();
                TaskManager.get().terminate();
                GlobalScreenUtils.unregister();
            }
        };
        stage.getInitialScene().enableAutoContentWidthHeight();

        stage.setTitle("Jarvis's Dictionary Client");

        MyScene myScene = new MyScene();

        FXUtils.observeHeight(stage.getInitialScene().getContentPane(), myScene.getNode(), 0);
        FXUtils.observeWidth(stage.getInitialScene().getContentPane(), myScene.getNode(), 0);

        HBox box = new HBox(new VBox(myScene.getNode()));
        stage.getInitialScene().getContentPane().getChildren().add(box);

        stage.getStage().setWidth(1280);
        stage.getStage().setHeight(800);
        stage.getStage().centerOnScreen();
        stage.getStage().show();
    }

    private static void disableVFXLogger() {
        Logger.setLogger(new Logger() {
            @Override
            public void _debug(String s) {
            }

            @Override
            public void _info(String s) {
            }

            @Override
            public void _warn(String s) {
            }

            @Override
            public void _error(String s, Throwable throwable) {
            }

            @Override
            public void _error(String s) {
            }
        });
    }

    @Override
    public void stop() {
        TCPInteractiveClient.getInstance().disconnect();
    }

    public static void main(String[] args) {
        disableVFXLogger();
        Theme.setTheme(new MyTheme());
        launch(args);
    }
}