package com.aymane.chatapp.dto;

import com.aymane.chatapp.model.MessageType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {

    private String roomId;
    private String sender;
    private String content;
    private MessageType type;
    private Instant timestamp;
}
