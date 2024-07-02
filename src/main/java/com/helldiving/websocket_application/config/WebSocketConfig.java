package com.helldiving.websocket_application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.lang.NonNull;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Messages sent to "/user" will be managed by the broker
        registry.enableSimpleBroker("/user");
        // Prefix for messages going to methods annotated with @MessageMapping
        // Messages from users will start with "/app" to reach these methods
        registry.setApplicationDestinationPrefixes("/app");
        // For user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    // Users will connect to "/ws" for WebSocket communication, SockJS is for browsers that don't support WebSocket)

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }
    // Configure message converters for WebSocket communication
    // Makes sure messages are converted to and from JSON format
    // Returning false allows other converters to be added if needed
    @Override
    public boolean configureMessageConverters(@NonNull List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        messageConverters.add(converter);

    // Return false because don't need more converters

        return false;
    }
}