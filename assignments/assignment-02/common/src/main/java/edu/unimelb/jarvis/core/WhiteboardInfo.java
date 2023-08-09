package edu.unimelb.jarvis.core;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class WhiteboardInfo implements Serializable {
    private static final long serialVersionUID = -8981130923929678335L;

    public WhiteboardInfo() {
        this.id = UUID.randomUUID().toString();
        this.participants = new ArrayList<>();
    }

    private String id;

    private User manager;

    private List<User> participants;
}

