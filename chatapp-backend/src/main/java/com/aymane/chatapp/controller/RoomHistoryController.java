package com.aymane.chatapp.controller;

import com.aymane.chatapp.dto.ChatMessageResponse;
import com.aymane.chatapp.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Historique des salons", description = "Consultation de l'historique des messages")

public class RoomHistoryController {

    private final ChatService chatService;

    public RoomHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "Récupère l'historique des messages d'un salon",
            description = "Retourne tous les messages d'un salon, triés par date croissante")
    public List<ChatMessageResponse> getHistory(@PathVariable String roomId) {
        return chatService.getRoomHistory(roomId);
    }
}
