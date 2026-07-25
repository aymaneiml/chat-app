package com.aymane.chatapp.service;

import com.aymane.chatapp.dto.ChatMessageRequest;
import com.aymane.chatapp.dto.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    ChatMessageResponse processIncomingMessage(String roomId, ChatMessageRequest request);
    ChatMessageResponse createSystemMessage(String roomId, String sender, String typeName);
    List<ChatMessageResponse> getRoomHistory(String roomId);
}
