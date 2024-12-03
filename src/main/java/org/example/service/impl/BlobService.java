package org.example.service.impl;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.models.user.DTOS.BlobInfoDTO;
import org.example.models.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class BlobService {
    private static final Logger log = LoggerFactory.getLogger(BlobService.class);

    private MediaService mediaService;
    private UserService userService;
    private  String connectionString;
    private  String containerName;

    public BlobService (@Value("${azureBlobStorageConnectionString}") String connectionString,
                        @Value("${azureBlobStorageContainerName}") String containerName,
                        MediaService mediaService,
                        UserService userService){
        this.connectionString = connectionString;
        this.containerName = containerName;
        this.mediaService = mediaService;
        this.userService = userService;
    }


    /*----GET SERVICE----*/
    public ResponseEntity<List<String>> getAllTurkeys() {
        List<String> videoUrls = new ArrayList<>();
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
        try {
            containerClient.listBlobs().forEach(blobItem -> {
                String sasUrl = generateSasForBlob(blobItem.getName());
                videoUrls.add(sasUrl);
            });
        } catch (Exception e) {
            log.error("Error listing blobs or generating SAS URLs", e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(videoUrls);
    }

    public String generateSasForBlob(String blobName) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusDays(1), // SAS token expiration time
                BlobContainerSasPermission.parse("r") // read permission
        );

        String sasToken = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }
    public List<String> getMediaReferencesOfUser(String jwt){
        User user =null;
        try {
            String jwtToken = jwt.replace("Bearer ", "");
            user =userService.getUserByUsername(getSubjectFromToken(jwtToken));
        }catch (Exception e){
            return new ArrayList<>();
        }
        return mediaService.getMediaReferencesByUserId(user);

    }

    /**GET SERVICE**/
    public ResponseEntity<List<BlobInfoDTO>> getBlobsByPrefix(String prefix) {

        List<BlobInfoDTO> blobInfos = new ArrayList<>();
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
        String prefijo = "";
        try {
            prefijo = getSubjectFromToken(prefix);
            for (BlobItem blobItem : containerClient.listBlobs(new ListBlobsOptions().setPrefix(prefijo), null)) {
                String sasUrl = generateSasForBlob(blobItem.getName());

                // Obtener metadatos del blob
                String fileName = containerClient.getBlobClient(blobItem.getName()).getProperties().getMetadata().get("fileName");
                log.info("metadata: "+fileName);
                BlobInfoDTO blobInfo = new BlobInfoDTO(sasUrl, fileName);
                blobInfos.add(blobInfo);
            }
        } catch (Exception e) {
            log.error("Error fetching blobs by prefix", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(blobInfos);
    }
    public ResponseEntity<List<String>> getBlobsByMetadata(String metadataKey, String metadataValue) {
        List<String> blobUrls = new ArrayList<>();
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        try {
            containerClient.listBlobs().forEach(blobItem -> {
                BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                Map<String, String> metadata = blobClient.getProperties().getMetadata();
                if (metadataValue.equals(metadata.get(metadataKey))) {
                    String sasUrl = generateSasForBlob(blobItem.getName());
                    blobUrls.add(sasUrl);
                }
            });
        } catch (Exception e) {
            log.error("Error fetching blobs by metadata", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(blobUrls);
    }
    /**GET SERVICE**/

    /*----GET SERVICE----*/
    private String getSubjectFromToken(String token) {
        token = token.replace("Bearer ", "");
        byte[] keyBytes= Decoders.BASE64.decode("7Px/YFH4C0kJnrZ3LOWxBebm8mx1KW7p/W6RVBgIo/4=");
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
    /*----DELETE SERVICE----*/
    /*----DELETE SERVICE----*/

}
