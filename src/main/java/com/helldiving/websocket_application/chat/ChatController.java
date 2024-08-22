package com.helldiving.websocket_application.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        List<ChatMessage> messages = chatMessageService.findChatMessages(senderId, recipientId);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String senderId = (principal != null) ? principal.getName() : chatMessage.getSenderId();
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        
        System.out.println("Processing message: " + savedMsg);

        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipientId(),
            "/queue/messages",
            savedMsg
        );
    }
}