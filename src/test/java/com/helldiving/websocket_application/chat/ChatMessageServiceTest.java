package com.helldiving.websocket_application.chat;

import com.helldiving.websocket_application.chatroom.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatMessageService service;

    @Test
    void save_ShouldSaveMessage_WhenValidMessage() {
        // Arrange
        ChatMessage message = ChatMessage.builder()
                .senderId("sender")
                .recipientId("recipient")
                .build();
        when(chatRoomService.getChatRoomId(anyString(), anyString(), anyBoolean())).thenReturn(Optional.of("chatId"));
        when(repository.save(any(ChatMessage.class))).thenReturn(message);

        // Act
        ChatMessage result = service.save(message);

        // Assert
        assertNotNull(result);
        verify(repository).save(message);
    }

    @Test
    void findChatMessages_ShouldReturnMessages_WhenChatExists() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().build());
        messages.add(ChatMessage.builder().build());

        when(chatRoomService.getChatRoomId(anyString(), anyString(), anyBoolean())).thenReturn(Optional.of("chatId"));
        when(repository.findByChatId(anyString())).thenReturn(messages);

        List<ChatMessage> result = service.findChatMessages("user1", "user2");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findByChatId("chatId");
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenChatDoesNotExist() {
        when(chatRoomService.getChatRoomId(anyString(), anyString(), anyBoolean())).thenReturn(Optional.empty());

        List<ChatMessage> result = service.findChatMessages("user1", "user2");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, never()).findByChatId(anyString());
    }

    @Test
    void saveSession_ShouldSaveSession() {
        ChatSession session = new ChatSession();
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(session);

        ChatSession savedSession = service.saveSession(session);

        assertNotNull(savedSession);
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void getSessionById_ShouldReturnSession_WhenSessionExists() {
        String sessionId = "testSessionId";
        ChatSession session = new ChatSession();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        ChatSession retrievedSession = service.getSessionById(sessionId);

        assertNotNull(retrievedSession);
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void getSessionById_ShouldThrowException_WhenSessionDoesNotExist() {
        String sessionId = "nonExistentSessionId";
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getSessionById(sessionId));
    }

    @Test
    void getActiveSessions_ShouldReturnActiveSessions() {
        List<ChatSession> activeSessions = new ArrayList<>();
        activeSessions.add(new ChatSession());
        activeSessions.add(new ChatSession());
        when(sessionRepository.findByStatus(SessionStatus.ACTIVE)).thenReturn(activeSessions);

        List<ChatSession> retrievedSessions = service.getActiveSessions();

        assertNotNull(retrievedSessions);
        assertEquals(2, retrievedSessions.size());
        verify(sessionRepository, times(1)).findByStatus(SessionStatus.ACTIVE);
    }
}