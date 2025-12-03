package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Query files request DTO
 */
@Data
@Schema(description = "Query files request")
public class QueryFilesRequest {

    @Schema(description = "File path filter", example = "/documents")
    private String path;

    @Schema(description = "Page number", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer page = 1;

    @Schema(description = "Page size (max 100)", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private Integer pageSize = 20;

    @Schema(description = "File type filter",
            example = "folder",
            allowableValues = {"folder", "image", "document", "video", "audio", "archive", "other"})
    private String type;

    @Schema(description = "Search keyword", example = "report")
    private String search;
}
