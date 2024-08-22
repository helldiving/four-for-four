package com.helldiving.websocket_application.chat;

import com.helldiving.websocket_application.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
    private final ChatSessionRepository sessionRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        if (chatMessage.getRecipientId() != null) {
            // Individual chat
            String chatId = chatRoomService
                    .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                    .orElseThrow(() -> new RuntimeException("Could not create chat room"));
            chatMessage.setChatId(chatId);
        } else {
            // Group chat
            chatMessage.setChatId("group_chat"); // create better way to identify group chats
        }
        return repository.save(chatMessage);
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        Optional<String> chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }

    public ChatSession saveSession(ChatSession chatSession) {
        return sessionRepository.save(chatSession);
    }

    public ChatSession getSessionById(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public List<ChatSession> getActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.ACTIVE);
    }
}