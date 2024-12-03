package org.example.controllers.privateControllers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("test")
@CrossOrigin
public class BlockBlobController {

    private static final Logger logger = LoggerFactory.getLogger(BlockBlobController.class);

    @Value("${azureBlobStorageConnectionString}")
    private String connectionString;

    @Value("${azureBlobStorageContainerName}")
    private String containerName;

    private BlobContainerClient connectStorage() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        return blobServiceClient.getBlobContainerClient(containerName);
    }

    @PostMapping("/uploadBlock")
    public ResponseEntity<String> uploadBlockC(@RequestParam("blobName") String blobName,
                                               @RequestParam("filePart") MultipartFile filePart,
                                               @RequestParam("blockId") String blockId) {
        try {
            // Log details
            logger.info("Received blockId: {}", blockId);
            logger.info("Received blobName: {}", blobName);
            logger.info("Received filePart size: {}", filePart.getSize());

            if (filePart.getSize() > 0) {
                String url = uploadBlock(blobName, filePart, blockId);
                logger.info("Block uploaded successfully. URL: {}", url);
                return new ResponseEntity<>(url, HttpStatus.OK);
            } else {
                logger.warn("Received empty filePart for blockId: {}", blockId);
                return new ResponseEntity<>("Empty file part", HttpStatus.BAD_REQUEST);
            }
        } catch (BlobStorageException e) {
            logger.error("BlobStorageException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.error("IOException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/commitBlocks")
    public ResponseEntity<String> commitBlocksC(@RequestParam("blobName") String blobName,
                                                @RequestParam("blockIds") List<String> blockIds) {
        try {
            logger.info("Committing blocks for blobName: {}", blobName);
            blockIds.forEach(blockId -> logger.info("Committing blockId: {}", blockId));

            String url = commitBlocks(blobName, blockIds);
            logger.info("Blocks committed successfully. URL: {}", url);
            return new ResponseEntity<>(url, HttpStatus.OK);
        } catch (BlobStorageException e) {
            logger.error("BlobStorageException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadBlock(String blobName, MultipartFile filePart, String blockId) throws IOException {
        BlobClient blobClient = connectStorage().getBlobClient(blobName);
        byte[] data = filePart.getBytes();
        logger.info("Uploading block ID: {} with size: {} bytes", blockId, data.length);
        try (InputStream inputStream = new BufferedInputStream(filePart.getInputStream())) {
            blobClient.getBlockBlobClient().stageBlock(blockId, inputStream, filePart.getSize());
        }catch (BlobStorageException e) {
            logger.error("Failed to upload block ID: {}. Error Code: {}, Message: {}", blockId, e.getStatusCode(), e.getMessage(), e);
        }
        return blobClient.getBlobUrl();
    }

    public String commitBlocks(String blobName, List<String> blockIds) {
        BlobClient blobClient = connectStorage().getBlobClient(blobName);
        blobClient.getBlockBlobClient().commitBlockList(blockIds);
        return blobClient.getBlobUrl();
    }
}
