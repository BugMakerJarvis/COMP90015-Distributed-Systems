package edu.unimelb.jarvis.server.websocket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.unimelb.jarvis.core.User;
import edu.unimelb.jarvis.core.WhiteboardInfo;
import edu.unimelb.jarvis.enums.MessageType;
import edu.unimelb.jarvis.enums.UserType;
import edu.unimelb.jarvis.server.service.UserManager;
import edu.unimelb.jarvis.server.service.WhiteboardManager;
import edu.unimelb.jarvis.server.ui.ServerController;
import edu.unimelb.jarvis.websocket.Message;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

@ServerEndpoint("/whiteboard")
public class WhiteboardServerEndpoint {
    private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<>());

    private static final Map<String, String> shapeListCache = new HashMap<>();

    private static final Map<String, String> backgroundImageCache = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New session opened: " + session.getId());
        // Add new session to the map of sessions
        sessions.put(session.getId(), session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message received from " + session.getId() + ": " + message);
        // Handle the received message
        Gson gson = new Gson();
        Type stringArrayType = new TypeToken<String[]>() {
        }.getType();
        Message receivedMessage = gson.fromJson(message, Message.class);
        String content = receivedMessage.getContent();
        User user = UserManager.getUserBySessionId(session.getId());
        switch (receivedMessage.getType()) {
            case CREATE_MANAGER:
                WhiteboardInfo whiteboard = new WhiteboardInfo();
                User manager = UserManager.addUser(new User(receivedMessage.getSender(), UserType.MANAGER, whiteboard.getId(), session.getId()));
                whiteboard.setManager(manager);
                WhiteboardManager.addWhiteboard(whiteboard);
                sendMessage(new Message(MessageType.CREATE_MANAGER, manager.getUsername(), "ADMIN", LocalDateTime.now()), session);
                // Broadcast the current users to all users in the same whiteboard
                broadcastMessage(new Message(MessageType.GET_ALL_USERS, gson.toJson(WhiteboardManager.getUsernamesInWhiteboard(whiteboard.getId())), "ADMIN", LocalDateTime.now()), session, true);
                ServerController.refreshTable();
                break;
            case CREATE_PARTICIPANT:
                // Send message to the manager to ask for permission
                // Get the manager's session
                WhiteboardInfo whiteboardToJoin = WhiteboardManager.getWhiteboardToJoin();
                Session managerSession = sessions.get(whiteboardToJoin.getManager().getSessionId());
                sendMessage(new Message(MessageType.CREATE_PARTICIPANT, gson.toJson(new String[]{receivedMessage.getSender(), whiteboardToJoin.getId(), session.getId()}), "ADMIN", LocalDateTime.now()), managerSession);
                break;
            case CREATE_PARTICIPANT_ACCEPTED:
                String[] combineArray1 = gson.fromJson(content, stringArrayType);
                String participantName = combineArray1[0];
                String whiteboardId = combineArray1[1];
                String participantSessionId = combineArray1[2];
                User participant = new User(participantName, UserType.PARTICIPANT, whiteboardId, participantSessionId);
                User finalParticipant = UserManager.addUser(participant);
                WhiteboardInfo whiteboardInfo = WhiteboardManager.addUserToWhiteboard(finalParticipant, whiteboardId);

                sendMessage(new Message(MessageType.CREATE_PARTICIPANT_ACCEPTED, finalParticipant.getUsername(), "ADMIN", LocalDateTime.now()), sessions.get(participantSessionId));

                broadcastMessage(new Message(MessageType.GET_ALL_USERS, gson.toJson(WhiteboardManager.getUsernamesInWhiteboard(whiteboardInfo.getId())), "ADMIN", LocalDateTime.now()), session, true);
                ServerController.refreshTable();
                break;
            case CREATE_PARTICIPANT_REJECTED:
                String[] combineArray2 = gson.fromJson(content, stringArrayType);
                sendMessage(new Message(MessageType.CREATE_PARTICIPANT_REJECTED, "Rejected", "ADMIN", LocalDateTime.now()), sessions.get(combineArray2[2]));
                break;
            case GET_ALL_WHITEBOARDS:
                List<WhiteboardInfo> whiteboards = WhiteboardManager.getAllWhiteboards();
                sendMessage(new Message(MessageType.GET_ALL_WHITEBOARDS, gson.toJson(whiteboards), "ADMIN", LocalDateTime.now()), session);
                break;
            case DRAWN:
                if (user == null) {
                    System.out.println("Message type is DRAWN but user is null");
                    return;
                }
                shapeListCache.put(user.getWhiteboardId(), content);
                broadcastMessage(new Message(MessageType.DRAWN, content, "ADMIN", LocalDateTime.now()), session, false);
                break;
            case INIT_WHITEBOARD:
                if (user == null) {
                    System.out.println("Message type is INIT_WHITEBOARD but user is null");
                    return;
                }
                sendMessage(new Message(MessageType.INIT_WHITEBOARD, gson.toJson(new String[]{shapeListCache.get(user.getWhiteboardId()), backgroundImageCache.get(user.getWhiteboardId())}), "ADMIN", LocalDateTime.now()), session);
                break;
            case OPEN_WHITEBOARD:
                if (user == null) {
                    System.out.println("Message type is OPEN_WHITEBOARD but user is null");
                    return;
                }
                backgroundImageCache.put(user.getWhiteboardId(), content);
                broadcastMessage(new Message(MessageType.OPEN_WHITEBOARD, content, "ADMIN", LocalDateTime.now()), session, false);
                break;
            case NEW_WHITEBOARD:
                if (user == null) {
                    System.out.println("Message type is NEW_WHITEBOARD but user is null");
                    return;
                }
                shapeListCache.remove(user.getWhiteboardId());
                backgroundImageCache.remove(user.getWhiteboardId());
                broadcastMessage(new Message(MessageType.NEW_WHITEBOARD, content, "ADMIN", LocalDateTime.now()), session, false);
                break;
            case KICK_PARTICIPANT:
                if (user == null || user.getType() != UserType.MANAGER) {
                    System.out.println("Message type is KICK_PARTICIPANT but user is null");
                    return;
                }
                User kickedUser = UserManager.getUserByUsername(content);
                if (kickedUser == null) {
                    System.out.println("User to be kicked is null");
                    return;
                }
                System.out.println("Kicking user " + kickedUser.getUsername());
                WhiteboardManager.kickUserFromWhiteboard(kickedUser.getWhiteboardId(), kickedUser.getId());
                sendMessage(new Message(MessageType.KICK_PARTICIPANT, "ADMIN", kickedUser.getUsername(), LocalDateTime.now()), sessions.get(kickedUser.getSessionId()));
                break;
            case CHAT_MESSAGE:
                if (user == null) {
                    System.out.println("Message type is CHAT_MESSAGE but user is null");
                    return;
                }
                broadcastMessage(new Message(MessageType.CHAT_MESSAGE, content, user.getUsername(), receivedMessage.getTimestamp()), session, false);
                break;
            default:
                break;
        }
    }

    public void broadcastMessage(Message message, Session session, boolean includingSelf) {
        User user = UserManager.getUserBySessionId(session.getId());
        if (user == null) {
            System.out.println("User is null");
            return;
        }
        String whiteboardId = user.getWhiteboardId();
        for (Session s : sessions.values()) {
            if (!s.isOpen()) continue;
            System.out.println("Broadcasting to " + s.getId());
            User u = UserManager.getUserBySessionId(s.getId());
            if (u != null && u.getWhiteboardId().equals(whiteboardId)) {
                if (includingSelf || !u.getId().equals(user.getId())) {
                    sendMessage(message, s);
                }
            }
        }
    }

    public void sendMessage(Message message, Session session) {
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

    public void broadcastShutdownMessage() {
        Message shutdownMessage = new Message(MessageType.SERVER_SHUTDOWN, "The server is shutting down. Please save your work and close the application.", "ADMIN", LocalDateTime.now());
        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                sendMessage(shutdownMessage, session);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session closed: " + session.getId());
        // Remove the closed session from the map of sessions
        String sessionId = session.getId();
        User user = UserManager.getUserBySessionId(sessionId);
        if (user != null) {
            WhiteboardInfo whiteboard = WhiteboardManager.getWhiteboard(user.getWhiteboardId());
            if (whiteboard != null) {
                if (user.getType() == UserType.MANAGER) {
                    WhiteboardManager.removeWhiteboard(whiteboard.getId());
                    // Kick all participants
                    for (User participant : whiteboard.getParticipants()) {
                        sendMessage(new Message(MessageType.KICK_PARTICIPANT, "ADMIN", participant.getUsername(), LocalDateTime.now()), sessions.get(participant.getSessionId()));
                    }
                } else {
                    WhiteboardManager.kickUserFromWhiteboard(whiteboard.getId(), user.getId());
                    // Broadcast the current users to all users in the same whiteboard
                    broadcastMessage(new Message(MessageType.GET_ALL_USERS, new Gson().toJson(WhiteboardManager.getUsernamesInWhiteboard(whiteboard.getId())), "ADMIN", LocalDateTime.now()), session, true);
                }
            }
            UserManager.removeUser(user.getId());
        }
        sessions.remove(sessionId);
        ServerController.refreshTable();
    }
}


