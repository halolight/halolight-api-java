package com.halolight.web.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create folder request DTO
 */
@Data
@Schema(description = "Create folder request")
public class CreateFolderRequest {

    @Schema(description = "Folder name", example = "Design Docs", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Folder name cannot be blank")
    private String name;

    @Schema(description = "Parent folder ID", example = "folder_root")
    private String parentId;
}
