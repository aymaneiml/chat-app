package com.aymane.chatapp;

import com.aymane.chatapp.dto.ChatMessageRequest;
import com.aymane.chatapp.dto.ChatMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatIntegrationTest {

    @LocalServerPort
    private int port;


    @Test
    void shouldBroadcastMessageToSubscribersOfSameRoom() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        BlockingQueue<ChatMessageResponse> received = new LinkedBlockingQueue<>();

        session.subscribe("/topic/room.general", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((ChatMessageResponse) payload);
            }
        });

        ChatMessageRequest request = new ChatMessageRequest();
        request.setSender("aymane");
        request.setContent("salut depuis le test");

        session.send("/app/chat.sendMessage/general", request);

        ChatMessageResponse result = received.poll(5, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo("aymane");
        assertThat(result.getContent()).isEqualTo("salut depuis le test");
        assertThat(result.getRoomId()).isEqualTo("general");
    }
}
