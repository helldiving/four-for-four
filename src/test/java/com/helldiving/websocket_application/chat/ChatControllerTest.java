package com.helldiving.websocket_application.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ChatMessageService chatMessageService;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void findChatMessages_ShouldReturnMessages_WhenMessagesExist() {
        List<ChatMessage> messages = Arrays.asList(new ChatMessage("Hello"), new ChatMessage("World"));
        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(messages);

        ResponseEntity<List<ChatMessage>> response = restTemplate.exchange(
                getRootUrl() + "/messages/user1/user2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ChatMessage>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenNoMessagesExist() {
        when(chatMessageService.findChatMessages(anyString(), anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<ChatMessage>> response = restTemplate.exchange(
                getRootUrl() + "/messages/user1/user2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ChatMessage>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}