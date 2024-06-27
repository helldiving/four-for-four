package com.helldiving.websocket_application.user;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository repository;

        // save user to database
    public void saveUser(User user) {
        user.setStatus(Status.ONLINE);
        repository.save(user);
    }
        // disconnect user from database
    public void disconnect(User user) {
       var storedUser = repository.findById(user.getNickName()).orElse(null);

       if (storedUser != null) {
        storedUser.setStatus(Status.OFFLINE);
        repository.save(storedUser);
       }
    }
        // get all users from database
    public List<User> findConnectedUsers() {
        
        return repository.findAllByStatus(Status.ONLINE);
    }
}
