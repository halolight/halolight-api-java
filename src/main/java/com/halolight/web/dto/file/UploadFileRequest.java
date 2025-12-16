package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Upload file request DTO
 */
@Data
@Schema(description = "Upload file request")
public class UploadFileRequest {

    @Schema(description = "File name", example = "document.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "File name cannot be blank")
    private String name;

    @Schema(description = "File path", example = "/documents")
    private String path;

    @Schema(description = "File size in bytes", example = "1024")
    @Positive(message = "File size must be positive")
    private Long size;

    @Schema(description = "MIME type", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Folder ID", example = "folder_123")
    private String folderId;
}
