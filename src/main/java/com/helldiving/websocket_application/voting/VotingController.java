package com.helldiving.websocket_application.voting;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class VotingController {
    private final SimpMessagingTemplate messagingTemplate;
    private final VotingService votingService;

    public VotingController(SimpMessagingTemplate messagingTemplate, VotingService votingService) {
        this.messagingTemplate = messagingTemplate;
        this.votingService = votingService;
    }

    @MessageMapping("/vote")
    public void handleVote(@Payload Vote vote) {
        votingService.recordVote(vote);
        
        if (votingService.isVotingComplete(vote.getSessionId())) {
            VotingResult result = votingService.getVotingResult(vote.getSessionId());
            
            result.getParticipants().forEach(participant -> 
                messagingTemplate.convertAndSendToUser(participant.getNickName(), "/queue/voteResult", result)
            );
        }
    }
}