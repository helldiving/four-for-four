package com.helldiving.websocket_application.chat;

import com.helldiving.websocket_application.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ChatSession {
    @Id
    private String id;
    private List<User> participants;
    private SessionStatus status;
    private String currentRule;
}