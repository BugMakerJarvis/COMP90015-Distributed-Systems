package edu.unimelb.jarvis.join.websocket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.unimelb.jarvis.core.WhiteboardInfo;
import edu.unimelb.jarvis.join.ui.JoinController;
import edu.unimelb.jarvis.websocket.Message;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@ClientEndpoint
public class WhiteboardJoinClientEndpoint {

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
            case CREATE_PARTICIPANT_ACCEPTED:
                System.out.println("Create participant accepted.");
                JoinController.updateUserName(content);
                JoinController.updateIsJoinAccepted(true);
                break;
            case CREATE_PARTICIPANT_REJECTED:
                JoinController.updateIsJoinAccepted(true);
                JoinController.showAlertAndExit("Oops!", "Your application to join the whiteboard has been rejected.");
                break;
            case GET_ALL_WHITEBOARDS:
                Type whiteboardListType = new TypeToken<List<WhiteboardInfo>>() {
                }.getType();
                List<WhiteboardInfo> whiteboardList = gson.fromJson(content, whiteboardListType);
                JoinController.updateAvailableWhiteboards(whiteboardList);
                break;
            case DRAWN:
                JoinController.updateShapes(content);
                break;
            case KICK_PARTICIPANT:
                JoinController.showAlertAndExit("Oops!", "You have been kicked out of the whiteboard.");
                break;
            case INIT_WHITEBOARD:
                Type stringArrayType = new TypeToken<String[]>() {
                }.getType();
                String[] combineArray = gson.fromJson(content, stringArrayType);
                String shapes = combineArray[0];
                String backgroundImage = combineArray[1];
                if (backgroundImage != null && !"".equals(backgroundImage))
                    JoinController.updateBackgroundImage(backgroundImage);
                if (shapes != null && !"".equals(shapes)) JoinController.updateShapes(shapes);
                break;
            case OPEN_WHITEBOARD:
                JoinController.updateBackgroundImage(content);
                break;
            case NEW_WHITEBOARD:
                JoinController.newWhiteboardCreated();
                break;
            case GET_ALL_USERS:
                Type stringListType = new TypeToken<List<String>>() {
                }.getType();
                JoinController.updateOnlineUserList(gson.fromJson(content, stringListType));
                break;
            case CHAT_MESSAGE:
                JoinController.updateChatRoom(receivedMessage.getSender(), content, receivedMessage.getTimestamp());
                break;
            case SERVER_SHUTDOWN:
                JoinController.showAlertAndExit("Oops!", "The server has been shut down.");
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
