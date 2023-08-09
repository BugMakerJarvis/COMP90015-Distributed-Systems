package edu.unimelb.jarvis.server.ui;

import edu.unimelb.jarvis.core.WhiteboardInfo;
import edu.unimelb.jarvis.core.User;
import edu.unimelb.jarvis.server.service.WhiteboardManager;
import edu.unimelb.jarvis.server.websocket.WhiteboardServerEndpoint;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.glassfish.tyrus.server.Server;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServerController extends Application {
    private final TableView<WhiteboardInfo> table = new TableView<>();
    private static final ObservableList<WhiteboardInfo> data = FXCollections.observableArrayList();
    private static int serverPort = 8080;
    private Server webSocketServer;

    public static void showAlertAndExit(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            System.exit(0);
        });
    }

    public void startWebSocketServer() {
        webSocketServer = new Server("127.0.0.1", serverPort, "", null, WhiteboardServerEndpoint.class);
        try {
            webSocketServer.start();
            System.out.println("WebSocket server started.");
        } catch (Exception e) {
            showAlertAndExit("WebSocket Server Error", "Failed to start the WebSocket server.");
        }
    }

    @Override
    public void start(Stage stage) {
        // Set the language to English
        Locale.setDefault(Locale.ENGLISH);
        // Start the WebSocket server
        startWebSocketServer();

        stage.setTitle("Whiteboard Server");
        stage.setWidth(600);
        stage.setHeight(500);
        stage.setResizable(false);

        // Configure the table columns
        TableColumn<WhiteboardInfo, String> whiteboardIdCol = new TableColumn<>("Whiteboard ID");
        whiteboardIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));

        TableColumn<WhiteboardInfo, String> managerCol = new TableColumn<>("Manager");
        managerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getManager().getUsername()));

        TableColumn<WhiteboardInfo, String> participantsCol = new TableColumn<>("Participants");

        // if participants is empty, display "No participants"
        participantsCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getParticipants().isEmpty() ? "No participants" :
                        data.getValue().getParticipants().stream().map(User::getUsername).collect(Collectors.joining(", ")))
        );

        table.getColumns().addAll(whiteboardIdCol, managerCol, participantsCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No whiteboards are currently open"));

        setColumnWidths(stage, whiteboardIdCol, 0.2);
        setColumnWidths(stage, managerCol, 0.3);
        setColumnWidths(stage, participantsCol, 0.5);

        setAlignmentAndCenterText(whiteboardIdCol, managerCol, participantsCol);

        Scene scene = new Scene(table);
        stage.setScene(scene);

        stage.show();
    }

    private void setColumnWidths(Stage stage, TableColumn<WhiteboardInfo, String> column, double widthPercentage) {
        column.prefWidthProperty().bind(stage.widthProperty().multiply(widthPercentage));
        column.minWidthProperty().bind(stage.widthProperty().multiply(widthPercentage));
    }

    @SafeVarargs
    private void setAlignmentAndCenterText(TableColumn<WhiteboardInfo, String>... columns) {
        for (TableColumn<WhiteboardInfo, String> column : columns) {
            column.setCellFactory(tc -> {
                TableCell<WhiteboardInfo, String> cell = new TableCell<>();
                cell.textProperty().bind(cell.itemProperty());
                cell.setAlignment(Pos.CENTER);
                return cell;
            });
        }
    }

    public static void refreshTable() {
        Platform.runLater(() -> {
            List<WhiteboardInfo> allWhiteboards = WhiteboardManager.getAllWhiteboards();
            data.clear();
            data.addAll(allWhiteboards);
            System.out.println("Refreshed table: " + allWhiteboards);
        });
    }

    @Override
    public void stop() throws Exception {
        // Broadcast the server shutdown message to all clients
        new WhiteboardServerEndpoint().broadcastShutdownMessage();
        System.out.println("Waiting for 5 seconds to allow all clients to receive the shutdown message...");
        Thread.sleep(5000);
        if (webSocketServer != null) {
            webSocketServer.stop();
        }
    }

    public static void main(String[] args) {
        // args[0] is the port number
        if (args.length > 0) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number: " + args[0]);
                System.exit(1);
            }
        }
        launch();
    }
}
