package edu.unimelb.jarvis.server.service;


import edu.unimelb.jarvis.core.User;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    // id -> user
    private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public static User addUser(User user) {
        // check if the username is already taken
        for (User u : users.values()) {
            if (u.getUsername().equals(user.getUsername())) {
                user.setUsername(user.getUsername() + "_" + UUID.randomUUID().toString().substring(0, 4));
                break;
            }
        }
        users.put(user.getId(), user);
        return user;
    }

    public static User removeUser(String id) {
        return users.remove(id);
    }

    public static User getUserBySessionId(String sessionId) {
        for (User user : users.values()) {
            if (user.getSessionId().equals(sessionId))
                return user;
        }
        return null;
    }

    public static User getUserByUsername(String username) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }
}


