package org.example.models.user.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlobInfoDTO {
    private String sasUrl;
    private String fileName;
}
