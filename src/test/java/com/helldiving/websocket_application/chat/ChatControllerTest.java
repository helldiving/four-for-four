package com.helldiving.websocket_application.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ChatMessageService chatMessageService;

    @LocalServerPort
    private int port;

    @Test
    void findChatMessages_ShouldReturnMessages_WhenMessagesExist() {
        List<ChatMessage> messages = Arrays.asList(new ChatMessage(), new ChatMessage());
        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(messages);

        ResponseEntity<ChatMessage[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/messages/user1/user2", ChatMessage[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenNoMessagesExist() {
        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<ChatMessage[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/messages/user1/user2", ChatMessage[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }
}