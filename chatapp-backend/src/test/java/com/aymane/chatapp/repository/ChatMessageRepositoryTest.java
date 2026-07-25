package com.aymane.chatapp.repository;

import com.aymane.chatapp.model.ChatMessage;
import com.aymane.chatapp.model.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void shouldFindMessagesByRoomIdOrderedByTimestamp(){
        repository.save(new ChatMessage(null,"general","aymane","salam", MessageType.CHAT, Instant.now()));
        repository.save(new ChatMessage(null,"random","aya","hi", MessageType.CHAT, Instant.now()));
        repository.save(new ChatMessage(null,"general","sara","hello", MessageType.CHAT, Instant.now()));

        List<ChatMessage> result = repository.findByRoomIdOrderByTimestampAsc("general");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSender()).isEqualTo("aymane");

    }
}
