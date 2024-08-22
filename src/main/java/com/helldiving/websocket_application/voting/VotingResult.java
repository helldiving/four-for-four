package com.helldiving.websocket_application.voting;

import com.helldiving.websocket_application.user.User;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VotingResult {
    private String sessionId;
    private Map<String, Integer> voteCounts;
    private List<User> participants;
}