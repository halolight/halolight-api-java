package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Batch delete files request DTO
 */
@Data
@Schema(description = "Batch delete files request")
public class BatchDeleteRequest {

    @Schema(description = "File IDs to delete", example = "[\"file_1\", \"file_2\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "File IDs cannot be empty")
    private List<String> ids;
}
