package org.example.controllers.privateControllers;

import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.Block;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.example.service.impl.UploadBlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("upload/media")
@CrossOrigin(exposedHeaders = "Filename")
public class UploadBlobController {
    private static final Logger log = LoggerFactory.getLogger(UploadBlobController.class);

    private UploadBlobService uploadBlobService;

    public UploadBlobController(UploadBlobService uploadBlobService) {
        this.uploadBlobService = uploadBlobService;
    }

    @PostMapping("/upload-chunk")
    public ResponseEntity<String> uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                              @RequestParam("fileName") String fileName,
                                              @RequestParam("index") int index,
                                              @RequestHeader("Authorization") String authorizationHeader) throws IOException {
        return uploadBlobService.uploadChunk(chunk, fileName, index, authorizationHeader);
    }

    @PostMapping("/commit-blocks")
    public ResponseEntity<String> commitBlocks(@RequestParam("fileName") String fileName,
                                               @RequestParam("blockList") String blockListJson,
                                               @RequestParam("metadata") String  metadata,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, String> metadataMap;
        List<String> blockList;

        try {
            metadataMap = objectMapper.readValue(metadata, HashMap.class);
            blockList = objectMapper.readValue(blockListJson, List.class);
            return uploadBlobService.commitBlocks(fileName, blockListJson, metadataMap, authorizationHeader);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        }

        //return ResponseEntity.ok("Blocks committed successfully");
    }
}
