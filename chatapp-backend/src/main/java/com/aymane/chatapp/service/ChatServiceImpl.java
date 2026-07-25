package com.aymane.chatapp.service;


import com.aymane.chatapp.dto.ChatMessageRequest;
import com.aymane.chatapp.dto.ChatMessageResponse;
import com.aymane.chatapp.mapper.ChatMessageMapper;
import com.aymane.chatapp.model.ChatMessage;
import com.aymane.chatapp.model.MessageType;
import com.aymane.chatapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
    private final ChatMessageRepository repository;
    private final ChatMessageMapper mapper;

    @Override
    public ChatMessageResponse processIncomingMessage(String roomId, ChatMessageRequest request) {

        ChatMessage entity = ChatMessage.builder()
                .roomId(roomId)
                .sender(request.getSender())
                .content(request.getContent())
                .type(MessageType.CHAT)
                .build();

        ChatMessage saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ChatMessageResponse createSystemMessage(String roomId, String sender, String typeName) {
        MessageType type = MessageType.valueOf(typeName);
        ChatMessage entity = ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .type(type)
                .build();
        ChatMessage saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public List<ChatMessageResponse> getRoomHistory(String roomId) {
        return repository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
