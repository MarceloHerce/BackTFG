package org.example.service.impl;

import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.Block;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class UploadBlobService {
    private static final Logger log = LoggerFactory.getLogger(UploadBlobService.class);

    private MediaService mediaService;
    private String connectionString;
    private String containerName;

    public UploadBlobService(@Value("${azureBlobStorageConnectionString}") String connectionString,
                             @Value("${azureBlobStorageContainerName}") String containerName,
                             MediaService mediaService) {
        this.connectionString = connectionString;
        this.containerName = containerName;
        this.mediaService = mediaService;
    }

    /*----UPLOAD SERVICE----*/
    public String generateBlockId(int index) {
        String rawBlockId = String.format("%05d-%s", index, UUID.randomUUID().toString());
        String blockId = Base64.getEncoder().encodeToString(rawBlockId.getBytes(StandardCharsets.UTF_8));
        log.info("Generated BlockId: " + blockId);
        return blockId;
    }

    public ResponseEntity<String> uploadChunk(MultipartFile chunk, String fileName, int index, String authorizationHeader) throws IOException {
        String blockId = generateBlockId(index);
        byte[] chunkBytes = chunk.getBytes();
        String storageFileName = "Default";
        try{
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            String username = getSubjectFromToken(jwtToken);
            storageFileName = mediaService.generateFileName(username);
            log.info("Username: " + username);
            log.info("SotrageFileName: " + storageFileName);

        }catch (Exception e) {
            log.error("Error committing block list", e);
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkBytes);
        BlockBlobClient blockBlobClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .blobName(fileName)
                .buildClient()
                .getBlockBlobClient();
        log.info("Uploading BlockId: " + blockId + " for file: " + fileName + " chunk: " + index);
        blockBlobClient.stageBlock(blockId, byteArrayInputStream, chunkBytes.length);
        log.info("Fin uploadChunk");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Filename", storageFileName);
        return ResponseEntity.ok().headers(headers).body(blockId);
    }

    public ResponseEntity<String> commitBlocks(String fileName, String blockListJson, HashMap<String,String> metadata,
                                               String authorizationHeader) {
        String storageFileName = "Default";
        try{
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            String username = getSubjectFromToken(jwtToken);
            storageFileName = mediaService.generateFileName(username);
            log.info("Username: " + username);
            log.info("SotrageFileName: " + storageFileName);
        }catch (Exception e) {
            log.error("Error committing block list", e);
        }
        log.info(storageFileName);
        log.info("Comienzo de commit blob");
        BlockBlobClient blockBlobClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .blobName(fileName)
                .buildClient()
                .getBlockBlobClient();
        BlockList blockList1 = blockBlobClient.listBlocks(BlockListType.ALL);
        log.info("Uncommitted Blocks:");
        for (Block block : blockList1.getUncommittedBlocks()) {
            log.info("Block ID: " + block.getName() + ", Size: " + block.getSizeLong());
        }

        log.info("Received blockListJson: " + blockListJson);
        String cleanedBlockListJson = blockListJson
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        List<String> blockList = List.of(cleanedBlockListJson.split(","));
        for (String blockId : blockList) {
            log.info("Block ID: " + blockId);
        }

        try {
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            String username = getSubjectFromToken(jwtToken);
            log.info(blockList.toString());
            blockBlobClient.commitBlockList(blockList);
            blockBlobClient.setMetadata(metadata);
            mediaService.setMediaReference(username, storageFileName);


        } catch (Exception e) {

            log.error("Error committing block list", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error committing block list: " + e.getMessage());
        }

        return ResponseEntity.ok("Upload complete");
    }
    /*----UPLOAD SERVICE----*/
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
