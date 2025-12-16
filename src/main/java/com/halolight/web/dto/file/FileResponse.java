package com.halolight.web.dto.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File response DTO
 * Unified structure for both files and folders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "File response")
public class FileResponse {

    @Schema(description = "File ID", example = "file_123")
    private String id;

    @Schema(description = "File name", example = "document.pdf")
    private String name;

    @Schema(description = "File type", example = "document",
            allowableValues = {"folder", "image", "document", "video", "audio", "archive", "other"})
    private String type;

    @Schema(description = "File size in bytes (null for folders)", example = "102400")
    private Long size;

    @Schema(description = "Number of items (for folders only)", example = "5")
    private Long items;

    @Schema(description = "File path", example = "/documents/file.pdf")
    private String path;

    @Schema(description = "MIME type", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Thumbnail URL", example = "https://picsum.photos/200/200")
    private String thumbnail;

    @Schema(description = "Is favorite", example = "false")
    private Boolean isFavorite;

    @Schema(description = "Creation timestamp", example = "2024-01-10T00:00:00Z")
    private String createdAt;

    @Schema(description = "Update timestamp", example = "2024-01-15T10:30:00Z")
    private String updatedAt;
}
