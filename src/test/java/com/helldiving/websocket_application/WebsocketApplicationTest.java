package com.helldiving.websocket_application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void testWebSocketConnection() throws Exception {
        BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

        String port = String.valueOf(testRestTemplate.getRestTemplate().getUriTemplateHandler()
                .expand("/").toString().split(":")[2].split("/")[0]);

        CompletableFuture<StompSession> sessionFuture = stompClient.connectAsync(getWsPath(port), new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders connectedHeaders) {
                session.subscribe("/user/queue/messages", new StompFrameHandler() {
                    @NonNull
                    @Override
                    public Type getPayloadType(@NonNull StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                        blockingQueue.add(payload != null ? payload.toString() : "");
                    }
                });

                session.send("/app/chat", "Hello, WebSocket!");
            }
        });

        StompSession session = sessionFuture.get(1, TimeUnit.SECONDS);
        assertNotNull(session);

        String receivedMessage = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.contains("Hello, WebSocket!"));
    }

    private String getWsPath(String port) {
        return String.format("ws://localhost:%s/ws", port);
    }
}