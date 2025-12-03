package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Rename file request DTO
 */
@Data
@Schema(description = "Rename file request")
public class RenameFileRequest {

    @Schema(description = "New file name", example = "new-document.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "File name cannot be blank")
    private String name;
}
