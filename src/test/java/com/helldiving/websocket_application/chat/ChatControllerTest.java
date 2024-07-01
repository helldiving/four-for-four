package com.helldiving.websocket_application.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void findChatMessages_ShouldReturnMessages_WhenMessagesExist() throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().build());
        messages.add(ChatMessage.builder().build());

        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(messages);

        mockMvc.perform(get("/messages/user1/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(chatMessageService, times(1)).findChatMessages("user1", "user2");
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenNoMessagesExist() throws Exception {
        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/messages/user1/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(chatMessageService, times(1)).findChatMessages("user1", "user2");
    }
}