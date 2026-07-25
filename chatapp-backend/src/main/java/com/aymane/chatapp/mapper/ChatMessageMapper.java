package com.aymane.chatapp.mapper;

import com.aymane.chatapp.dto.ChatMessageResponse;
import com.aymane.chatapp.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageResponse toResponse(ChatMessage entity){
        ChatMessageResponse response = new ChatMessageResponse();
        return response.builder()
                .roomId(entity.getRoomId())
                .sender(entity.getSender())
                .content(entity.getContent())
                .type(entity.getType())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
