package com.helldiving.websocket_application.chat;

import java.util.ArrayList;
import java.util.List;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.helldiving.websocket_application.chatroom.ChatRoomService;


@Service
@RequiredArgsConstructor

public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow(); // placement for dedicated exception as option
        chatMessage.setChatId(chatId);
        repository.save(chatMessage);

        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);

        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }
}
