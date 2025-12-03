package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Move file request DTO
 */
@Data
@Schema(description = "Move file request")
public class MoveFileRequest {

    @Schema(description = "Target folder path", example = "/new-folder", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Target path cannot be blank")
    private String targetPath;
}
