package com.aymane.chatapp.service;

import com.aymane.chatapp.dto.ChatMessageRequest;
import com.aymane.chatapp.dto.ChatMessageResponse;
import com.aymane.chatapp.mapper.ChatMessageMapper;
import com.aymane.chatapp.model.ChatMessage;
import com.aymane.chatapp.model.MessageType;
import com.aymane.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatServiceImplTest {

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private ChatMessageMapper mapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void shouldProcessIncommingMessageAndReturnResponse() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSender("ayamne");
        request.setContent("salam tout le monde");

        ChatMessage savedEntity = ChatMessage.builder()
                .roomId("general")
                .sender("aymane")
                .content("salam tout le monde")
                .type(MessageType.CHAT)
                .build();
        ChatMessageResponse expectedRsponse = new ChatMessageResponse(
                "general","aymane","salam tout le monde", MessageType.CHAT, Instant.now()
        );

        when(repository.save(any(ChatMessage.class))).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedRsponse);

        ChatMessageResponse result = chatService.processIncomingMessage("general", request);

        assertThat(result.getSender()).isEqualTo("aymane");
        assertThat(result.getRoomId()).isEqualTo("general");
    }
}
