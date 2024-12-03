package org.example.service.impl;

import org.example.models.user.User;
import org.example.models.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.example.exceptions.UserNotFoundException;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    User getUserByUsername(String username) throws UserNotFoundException{
        Optional<User> user = userRepository.findByUserName(username);
            return user.orElseThrow(() -> {
                log.warn("User with username {} not found", username);
                return new UserNotFoundException("User with username " + username + " not found");
            });
    }
}
