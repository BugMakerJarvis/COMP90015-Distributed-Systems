package edu.unimelb.jarvis.websocket;

import edu.unimelb.jarvis.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private MessageType type;
    private String content;
    private String sender;
    private LocalDateTime timestamp;
}
