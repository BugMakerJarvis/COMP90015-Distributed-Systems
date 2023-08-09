package edu.unimelb.jarvis.join.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.unimelb.jarvis.core.WhiteboardInfo;
import edu.unimelb.jarvis.enums.MessageType;
import edu.unimelb.jarvis.enums.ShapeType;
import edu.unimelb.jarvis.graph.*;
import edu.unimelb.jarvis.join.websocket.WhiteboardJoinClientEndpoint;
import edu.unimelb.jarvis.util.Base64Util;
import edu.unimelb.jarvis.websocket.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class JoinController extends Application {
    private static String username = "Anonymous";
    private static String serverAddress = "127.0.0.1";
    private static int serverPort = 8080;
    private static boolean isJoinAccepted = false;
    private static DrawTool drawTool;
    private static ListView<String> onlineUserList;
    private static TextArea chatRoom;
    private Canvas canvas;
    private GraphicsContext gc;
    private ShapeType currentShapeType = ShapeType.PEN;
    private Pen currentPen;
    private double startX, startY;
    private boolean isDragging = false;
    private WhiteboardJoinClientEndpoint joinEndpoint;
    private Session webSocketSession;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(Shape.class, new ShapeTypeAdapter())
            .create();
    private static List<WhiteboardInfo> availableWhiteboards;

    public static void updateUserName(String newUserName) {
        username = newUserName;
    }

    public static void updateOnlineUserList(List<String> userList) {
        Platform.runLater(() -> {
            List<String> updatedUserList = userList.stream()
                    .map(user -> user.equals(username) ? user + " (self)" : user)
                    .sorted((user1, user2) -> {
                        if (user1.endsWith("(self)")) {
                            return -1;
                        } else if (user2.endsWith("(self)")) {
                            return 1;
                        } else {
                            return user1.compareTo(user2);
                        }
                    })
                    .collect(Collectors.toList());
            onlineUserList.getItems().setAll(updatedUserList);
        });
    }

    public static void updateAvailableWhiteboards(List<WhiteboardInfo> newAvailableWhiteboards) {
        availableWhiteboards = newAvailableWhiteboards;
    }

    public static void updateChatRoom(String username, String message, LocalDateTime timestamp) {
        Platform.runLater(() -> {
            if (!chatRoom.getText().isEmpty()) {
                chatRoom.appendText("\n"); // Add a newline before the new message
            }
            chatRoom.appendText(String.format("[%s] %s: %s", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), username, message));
        });
    }

    public static void updateShapes(String content) {
        System.out.println("Received shapes: " + content);
        Platform.runLater(() -> {
            Type shapeListType = new TypeToken<List<Shape>>() {
            }.getType();
            List<Shape> shapes = gson.fromJson(content, shapeListType);
            drawTool.setShapes(shapes);
            drawTool.redrawShapes();
        });
    }

    public static void updateBackgroundImage(String content) {
        Platform.runLater(() -> {
            Image image = Base64Util.base64StringToImage(content);
            drawTool.clearAll();
            drawTool.setBackgroundImage(image);
            drawTool.redrawBackgroundImage();
        });
    }

    public static void updateIsJoinAccepted(boolean isAccepted) {
        isJoinAccepted = isAccepted;
    }

    public static void newWhiteboardCreated() {
        Platform.runLater(() -> {
            drawTool.clearAll();
        });
    }

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

    private void connectWebSocketServer() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            joinEndpoint = new WhiteboardJoinClientEndpoint();
            webSocketSession = container.connectToServer(joinEndpoint, new URI(String.format("ws://%s:%d/whiteboard", serverAddress, serverPort)));

            joinEndpoint.sendMessage(new Message(MessageType.GET_ALL_WHITEBOARDS, null, null, LocalDateTime.now()));
            // Wait for the server to respond with the available whiteboards
            while (availableWhiteboards == null) {
                Thread.sleep(100);
            }

            if (availableWhiteboards.isEmpty()) {
                showAlertAndExit("No available whiteboard!", "There are no available whiteboards to join.");
            }
        } catch (Exception e) {
            showAlertAndExit("Connection Error", "Failed to connect to the server.");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Set the language to English
        Locale.setDefault(Locale.ENGLISH);

        // Connect to the WebSocket server
        connectWebSocketServer();

        // Send a message to the server to register the user
        joinEndpoint.sendMessage(new Message(MessageType.CREATE_PARTICIPANT, null, username, LocalDateTime.now()));

        // Create a BorderPane layout to hold the UI components
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1280, 720);

        // Create the canvas
        canvas = new Canvas(960, 720);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawTool = new DrawTool(gc);

        // Initialize the whiteboard
        while (!isJoinAccepted) {
            System.out.println("Waiting for join acceptance...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        joinEndpoint.sendMessage(new Message(MessageType.INIT_WHITEBOARD, null, null, LocalDateTime.now()));

        // Add event listeners for mouse and keyboard input
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);

        // Create the chat room
        chatRoom = new TextArea();
        chatRoom.setEditable(false);
        chatRoom.setWrapText(true);
        chatRoom.setPrefHeight(420);


        // Add a TextField for sending messages
        TextField messageField = new TextField();
        messageField.setPrefHeight(40);
        messageField.setPromptText("Enter your message here");
        messageField.setOnAction(event -> {
            // Send the message to the server
            String message = messageField.getText();
            // Update chatArea with the new message
            if (!chatRoom.getText().isEmpty()) {
                chatRoom.appendText("\n"); // Add a newline before the new message
            }
            // Add the new message, username, and timestamp (YYYY-MM-DD HH:MM:SS)
            chatRoom.appendText(String.format("[%s] %s: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), username, message));
            messageField.clear();
            joinEndpoint.sendMessage(new Message(MessageType.CHAT_MESSAGE, message, username, LocalDateTime.now()));
        });

        // Create the online users list
        onlineUserList = new ListView<>();
        onlineUserList.setPrefHeight(260);

        ContextMenu contextMenu = new ContextMenu();

        onlineUserList.setContextMenu(contextMenu);
        onlineUserList.setOnContextMenuRequested(event -> contextMenu.show(onlineUserList, event.getScreenX(), event.getScreenY()));

        VBox chatBox = new VBox(onlineUserList, chatRoom, messageField);

        // Create the toolbar
        ComboBox<ShapeType> shapeTypeComboBox = new ComboBox<>();
        shapeTypeComboBox.getItems().addAll(ShapeType.values());
        shapeTypeComboBox.setValue(ShapeType.PEN);
        shapeTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentShapeType = newValue;
            System.out.println("Shape type changed to " + newValue);
        });
        ToolBar toolBar = new ToolBar(shapeTypeComboBox, drawTool.getColorPicker());
        toolBar.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0), CornerRadii.EMPTY, Insets.EMPTY)));
        // Add a region to push the toolbar to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbarContainer = new HBox(spacer, toolBar);

        // Combine the canvas and toolbar in a VBox
        VBox canvasContainer = new VBox(toolbarContainer, canvas);

        // Add the canvas and chat room to the root pane
        root.setLeft(canvasContainer);
        root.setRight(chatBox);

        stage.setTitle("Whiteboard - Participant - " + username);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();

        if (currentShapeType == ShapeType.TEXT) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter Text");
            dialog.setHeaderText(null);
            dialog.setContentText("Please enter the text:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(text -> {
                drawTool.drawShape(new Text(startX, startY, text, drawTool.getColorPicker().getValue()));
                System.out.println("Text drawn: " + text);
                joinEndpoint.sendMessage(new Message(MessageType.DRAWN, gson.toJson(drawTool.getShapes()), username, LocalDateTime.now()));
            });
        } else if (currentShapeType == ShapeType.PEN) {
            isDragging = true;
            currentPen = new Pen(drawTool.getColorPicker().getValue());
            currentPen.addPoint(startX, startY);
        } else {
            isDragging = true;
            gc.save(); // Save the current state of the graphics context
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!isDragging) {
            return;
        }

        isDragging = false;
        double endX = event.getX();
        double endY = event.getY();

        if (currentShapeType == ShapeType.PEN) {
            drawTool.drawShape(currentPen);
            currentPen = null;
        } else {
            gc.restore(); // Restore the saved state to finalize the drawing
            switch (currentShapeType) {
                case LINE:
                    drawTool.drawShape(new Line(startX, startY, endX, endY, drawTool.getColorPicker().getValue()));
                    break;
                case RECTANGLE:
                    double rectWidth = Math.abs(endX - startX);
                    double rectHeight = Math.abs(endY - startY);
                    drawTool.drawShape(new Rectangle(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight, drawTool.getColorPicker().getValue()));
                    break;
                case CIRCLE:
                    double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                    drawTool.drawShape(new Circle(startX, startY, radius, drawTool.getColorPicker().getValue()));
                    break;
                case ELLIPSE:
                    double radiusX = Math.abs(endX - startX) / 2;
                    double radiusY = Math.abs(endY - startY) / 2;
                    drawTool.drawShape(new Ellipse(startX + radiusX, startY + radiusY, radiusX, radiusY, drawTool.getColorPicker().getValue()));
                    break;
                default:
                    break;
            }
        }
        System.out.println("Shape drawn: " + currentShapeType);
        joinEndpoint.sendMessage(new Message(MessageType.DRAWN, gson.toJson(drawTool.getShapes()), username, LocalDateTime.now()));
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!isDragging) {
            return;
        }

        double endX = event.getX();
        double endY = event.getY();

        // Clear the canvas and restore the saved state
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        drawTool.redrawAll();

        if (currentShapeType == ShapeType.PEN) {
            currentPen.addPoint(endX, endY);
            currentPen.draw(gc);
            return;
        }

        gc.restore();
        gc.save();
        switch (currentShapeType) {
            case LINE:
                drawTool.drawLinePreview(startX, startY, endX, endY);
                break;
            case RECTANGLE:
                double rectWidth = Math.abs(endX - startX);
                double rectHeight = Math.abs(endY - startY);
                drawTool.drawRectanglePreview(Math.min(startX, endX), Math.min(startY, endY), rectWidth, rectHeight);
                break;
            case CIRCLE:
                double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                drawTool.drawCirclePreview(startX, startY, radius);
                break;
            case ELLIPSE:
                double radiusX = Math.abs(endX - startX) / 2;
                double radiusY = Math.abs(endY - startY) / 2;
                drawTool.drawEllipsePreview(startX + radiusX, startY + radiusY, radiusX, radiusY);
                break;
            default:
                break;
        }
    }

    @Override
    public void stop() throws Exception {
        if (webSocketSession != null) {
            webSocketSession.close();
        }
    }

    public static void main(String[] args) {
        // args[0] is the server address, args[1] is the port number, args[2] is the username
        if (args.length != 3) {
            System.out.println("Usage: java -jar Whiteboard.jar <server address> <port number> <username>");
            System.out.println("Using default values: 127.0.0.1 8080 Anonymous");
        } else {
            // Check if the port number is valid
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number: " + args[1]);
                System.exit(1);
            }
            serverAddress = args[0];
            username = args[2];
        }
        launch();
    }
}