package com.helldiving.websocket_application.chat;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    List<ChatSession> findByStatus(SessionStatus status);
}