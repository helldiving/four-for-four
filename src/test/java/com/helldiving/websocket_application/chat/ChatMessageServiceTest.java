package com.helldiving.websocket_application.chat;

import com.helldiving.websocket_application.chatroom.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private ChatRoomService chatRoomService;

    private ChatMessageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ChatMessageService(repository, chatRoomService);
    }

    @Test
    void save_ShouldSaveMessage_WhenValidMessage() {
        ChatMessage message = ChatMessage.builder()
                .senderId("user1")
                .recipientId("user2")
                .content("Test message")
                .build();

        when(chatRoomService.getChatRoomId(anyString(), anyString(), anyBoolean())).thenReturn(Optional.of("chatId"));
        when(repository.save(any(ChatMessage.class))).thenReturn(message);

        ChatMessage savedMessage = service.save(message);

        assertNotNull(savedMessage);
        assertEquals("chatId", savedMessage.getChatId());
        verify(repository, times(1)).save(message);
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
}