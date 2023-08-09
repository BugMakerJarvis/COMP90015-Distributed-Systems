package edu.unimelb.jarvis.server.service;

import edu.unimelb.jarvis.core.User;
import edu.unimelb.jarvis.core.WhiteboardInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WhiteboardManager {
    // id -> whiteboard
    private static final ConcurrentHashMap<String, WhiteboardInfo> whiteboards = new ConcurrentHashMap<>();

    public static void addWhiteboard(WhiteboardInfo whiteboard) {
        whiteboards.put(whiteboard.getId(), whiteboard);
    }

    public static WhiteboardInfo getWhiteboard(String id) {
        return whiteboards.get(id);
    }

    public static WhiteboardInfo removeWhiteboard(String id) {
        return whiteboards.remove(id);
    }

    public static boolean whiteboardExists(String id) {
        return whiteboards.containsKey(id);
    }

    public static List<WhiteboardInfo> getAllWhiteboards() {
        return List.copyOf(whiteboards.values());
    }

    public static WhiteboardInfo kickUserFromWhiteboard(String whiteboardId, String userId) {
        WhiteboardInfo whiteboard = getWhiteboard(whiteboardId);
        if (whiteboard != null) {
            whiteboard.getParticipants().removeIf(user -> user.getId().equals(userId));
        }
        return whiteboard;
    }

    public static WhiteboardInfo addUserToWhiteboard(User user, String whiteboardId) {
        // Choose a whiteboard with the least number of participants
        WhiteboardInfo whiteboard = getWhiteboard(whiteboardId);
        if (whiteboard != null) {
            whiteboard.getParticipants().add(user);
        }
        return whiteboard;
    }

    public static WhiteboardInfo getWhiteboardToJoin() {
        // Choose a whiteboard with the least number of participants
        return whiteboards.values().stream().min(Comparator.comparingInt(w -> w.getParticipants().size())).orElse(null);
    }

    public static List<User> getUsersInWhiteboard(String whiteboardId) {
        WhiteboardInfo wb = whiteboards.get(whiteboardId);
        List<User> users = new ArrayList<>(wb.getParticipants());
        users.add(wb.getManager());
        return users;
    }

    public static List<String> getUsernamesInWhiteboard(String whiteboardId) {
        List<String> usernames = new ArrayList<>();
        for (User user : getUsersInWhiteboard(whiteboardId)) {
            usernames.add(user.getUsername());
        }
        return usernames;
    }
}

