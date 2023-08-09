package edu.unimelb.jarvis.create.websocket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.unimelb.jarvis.create.ui.CreateController;
import edu.unimelb.jarvis.websocket.Message;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@ClientEndpoint
public class WhiteboardCreateClientEndpoint {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket client connected: " + session.getId());
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        // Handle the received message
        Gson gson = new Gson();
        Message receivedMessage = gson.fromJson(message, Message.class);
        String content = receivedMessage.getContent();
        switch (receivedMessage.getType()) {
            case CREATE_MANAGER:
                CreateController.updateUserName(content);
                break;
            case CREATE_PARTICIPANT:
                CreateController.showAcceptParticipantDialog(content);
                break;
            case DRAWN:
                CreateController.updateShapes(content);
                break;
            case GET_ALL_USERS:
                System.out.println("Received all users: " + content);
                CreateController.updateOnlineUserList(content);
                break;
            case CHAT_MESSAGE:
                CreateController.updateChatRoom(receivedMessage.getSender(), content, receivedMessage.getTimestamp());
                break;
            case SERVER_SHUTDOWN:
                CreateController.showAlertAndExit("Oops!", "The server has been shut down.");
                break;
            default:
                break;
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Closing a WebSocket due to " + closeReason.getReasonPhrase());
        this.session = null;
    }

    @OnError
    public void onError(Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        error.printStackTrace();
    }

    public void sendMessage(Message message) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(message);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        if (session != null) {
            session.close();
        }
    }
}

