package org.example.service.impl;

import org.example.exceptions.UserNotFoundException;
import org.example.models.user.Media;
import org.example.models.user.MediaRepository;
import org.example.models.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {
    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private MediaRepository mediaRepository;
    private UserService userService;

    public MediaService(MediaRepository mediaRepository, UserService userService) {
        this.mediaRepository = mediaRepository;
        this.userService = userService;
    }

    void setMediaReference(String username, String mediaReference) {
        User user = null;
        try {
            user = userService.getUserByUsername(username);
        } catch (UserNotFoundException e) {
            log.error("User not found: " + e);
        }
        if (username != null) {
            mediaRepository.save(new Media(0L, user, mediaReference));
        }
    }
    public String generateFileName(String username) {
        String uniqueID = UUID.randomUUID().toString();

        String fileName = username + "_" + uniqueID + ".webm";
        return fileName;
    }
    public List<String> getMediaReferencesByUserId(User user) {
        return mediaRepository.findMediaReferencesByUserId(user.getUserId());
    }
}
