package edu.unimelb.jarvis.core;

import edu.unimelb.jarvis.enums.UserType;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class User implements Serializable {
    private static final long serialVersionUID = 3599775033100391435L;

    public User(String username, UserType type, String whiteboardId, String sessionId) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.type = type;
        this.whiteboardId = whiteboardId;
        this.sessionId = sessionId;
    }

    private String id;

    private String username;

    private UserType type;

    private String whiteboardId;

    private String sessionId;
}