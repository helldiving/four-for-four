package com.helldiving.websocket_application.matching;

import com.helldiving.websocket_application.user.User;
import com.helldiving.websocket_application.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class UserMatchingService {
    private final UserRepository userRepository;
    private final ConcurrentLinkedQueue<User> waitingUsers = new ConcurrentLinkedQueue<>();

    public UserMatchingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUserToWaitingQueue(User user) {
        waitingUsers.offer(user);
        matchUsers();
    }

    private void matchUsers() {
        if (waitingUsers.size() >= 4) {
            List<User> matchedUsers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                matchedUsers.add(waitingUsers.poll());
            }
            createChatSession(matchedUsers);
        }
    }

    private void createChatSession(List<User> users) {
        // remember:
        // create a new chat session for the matched users
        // this involves creating a new ChatSession object and saving it to the database
        // also need to notify the matched users that a session has been created
    }
}