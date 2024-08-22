package com.helldiving.websocket_application;

import com.helldiving.websocket_application.chat.ChatMessage;
import com.helldiving.websocket_application.chat.ChatNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        var webSocketClient = new StandardWebSocketClient();
        var sockJsClient = new SockJsClient(List.of(new WebSocketTransport(webSocketClient)));
        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void testIndividualMessage() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<ChatNotification> completableFuture = new CompletableFuture<>();

        var sessionHandler = new TestStompSessionHandler(completableFuture);
        var headers = new WebSocketHttpHeaders();

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", headers, sessionHandler)
                .get(5, SECONDS);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("Test Message");
        chatMessage.setSenderId("sender");
        chatMessage.setRecipientId("recipient");

        session.send("/app/chat", chatMessage);

        ChatNotification chatNotification = completableFuture.get(5, SECONDS);

        assertNotNull(chatNotification);
        assertTrue(chatNotification.getContent().contains("Test Message"));
    }

    private class TestStompSessionHandler extends StompSessionHandlerAdapter {
        private final CompletableFuture<ChatNotification> completableFuture;

        public TestStompSessionHandler(CompletableFuture<ChatNotification> completableFuture) {
            this.completableFuture = completableFuture;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe("/user/queue/messages", this);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((ChatNotification) payload);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatNotification.class;
        }
    }
}