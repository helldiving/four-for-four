package com.helldiving.websocket_application.rules;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RuleManager {
    private final List<String> possibleRules = List.of(
        "Only speak in questions",
        "Start every sentence with the last word of the previous message",
        "Communicate using only emojis",
        "Pretend you're all secret agents on a mission"
    );

    public String getRandomRule() {
        Random random = new Random();
        return possibleRules.get(random.nextInt(possibleRules.size()));
    }

    public boolean validateMessage(String message, String currentRule) {
        // rule validation logic here
        return true;
    }
}