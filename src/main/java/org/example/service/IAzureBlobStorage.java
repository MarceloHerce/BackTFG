package org.example.service;

import com.azure.storage.blob.BlobContainerClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAzureBlobStorage {
    ResponseEntity<InputStreamResource> getMediaFromStorage(String mediaRef);
    ResponseEntity<String> uploadMediaToStorage(MultipartFile file);
    BlobContainerClient connectToStorage(String connectionString, String containerName);
}
