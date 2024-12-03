package org.example.controllers.privateControllers;

import org.example.models.user.DTOS.BlobInfoDTO;
import org.example.service.impl.BlobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("get/media")
@CrossOrigin
public class GetBlobController {
    private BlobService blobService;
    public GetBlobController (BlobService blobService) {
        this.blobService = blobService;
    }
    @GetMapping(value = "")
    public List<String> getMediareferencesOfUser(@RequestHeader("Authorization") String authorizationHeader){
        return blobService.getMediaReferencesOfUser(authorizationHeader);
    }
    @GetMapping(value = "/user")
    public ResponseEntity<List<BlobInfoDTO>> getMediareferencesOfUserCorrect(@RequestHeader("Authorization") String authorizationHeader){
        return blobService.getBlobsByPrefix(authorizationHeader);
    }
}
