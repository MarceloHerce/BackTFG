package org.example.service;

import org.example.models.user.Media;
import org.example.models.user.User;

import java.util.List;

public interface IMediaService {
    void saveMediaReference(Long userId);
    List<Media> getAllMedia(Long userId);
}
