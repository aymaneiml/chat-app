package com.aymane.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    @NotBlank(message = "le nom de l'expediteur est requis")
    private String sender;

    @NotBlank(message = "le contenu du message ne peut pas etre vide")
    private String content;
}
