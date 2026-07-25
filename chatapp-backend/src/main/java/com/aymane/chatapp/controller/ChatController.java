package com.aymane.chatapp.controller;

import com.aymane.chatapp.dto.ChatMessageRequest;
import com.aymane.chatapp.dto.ChatMessageResponse;
import com.aymane.chatapp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room.{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable String roomId, @Valid ChatMessageRequest request){
        return chatService.processIncomingMessage(roomId,request);
    }

    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/room.{roomId}")
    public ChatMessageResponse joinRoom(@DestinationVariable String roomId, @Valid ChatMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {

        // On mémorise qui est connecté et dans quel salon, pour le retrouver à la déconnexion
        headerAccessor.getSessionAttributes().put("username", request.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        return chatService.createSystemMessage(roomId, request.getSender(),"JOIN");
    }
}
