package com.helldiving.websocket_application.voting;

import lombok.Data;

@Data
public class Vote {
    private String sessionId;
    private String userId;
    private String voteOption;
}