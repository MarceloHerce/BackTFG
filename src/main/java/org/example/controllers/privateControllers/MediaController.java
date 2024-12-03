package org.example.controllers.privateControllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.service.impl.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;

@RestController
@RequestMapping("/media")
@CrossOrigin(exposedHeaders = "Filename")
public class MediaController {
    private MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/name")
    public ResponseEntity<String> getMediaNameStorage(@RequestHeader("Authorization") String authorizationHeader)
            throws IOException {
        try{
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            String username = getSubjectFromToken(jwtToken);
            return ResponseEntity.ok().body(mediaService.generateFileName(username));
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
    private String getSubjectFromToken(String token) {
        byte[] keyBytes= Decoders.BASE64.decode("7Px/YFH4C0kJnrZ3LOWxBebm8mx1KW7p/W6RVBgIo/4=");
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
