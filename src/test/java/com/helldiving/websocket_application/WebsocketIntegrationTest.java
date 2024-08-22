package com.helldiving.websocket_application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helldiving.websocket_application.chat.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketIntegrationTest.class);

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        this.stompClient = new WebSocketStompClient(sockJsClient);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(converter);
    }

    @Test
    void testIndividualMessage() throws InterruptedException, ExecutionException, TimeoutException {
        StompSession session = null;
        try {
            CompletableFuture<ChatMessage> completableFuture = new CompletableFuture<>();

            String url = String.format("ws://localhost:%d/ws", port);
            logger.info("Connecting to URL: {}", url);

            StompSessionHandler sessionHandler = new LoggingStompSessionHandler();

            session = stompClient
                    .connect(url, new WebSocketHttpHeaders(), sessionHandler, this.port)
                    .get(15, SECONDS);

            logger.info("Session established: {}", session.isConnected());

            String subscriptionPath = "/user/queue/messages";
            logger.info("Subscribing to: {}", subscriptionPath);

            session.subscribe(subscriptionPath, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ChatMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    logger.info("Received message: {}", payload);
                    completableFuture.complete((ChatMessage) payload);
                }
            });

            ChatMessage message = new ChatMessage("Test Message");
            message.setSenderId("sender");
            message.setRecipientId("recipient");

            logger.info("Sending message: {}", message);
            session.send("/app/chat", message);

            ChatMessage receivedMessage = completableFuture.get(15, SECONDS);

            logger.info("Received message: {}", receivedMessage);

            assertNotNull(receivedMessage);
            assertEquals("Test Message", receivedMessage.getContent());
        } finally {
            if (session != null && session.isConnected()) {
                logger.info("Disconnecting session");
                session.disconnect();
            }
        }
    }

    private class LoggingStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            logger.info("New session established : {}", session.getSessionId());
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            logger.error("Got an exception", exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            logger.error("Got a transport error", exception);
        }
    }
}