package com.helldiving.websocket_application.user;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>{

    List<User> findAllByStatus(Status status);

}
