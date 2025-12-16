package com.halolight.web.dto.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Update folder request DTO
 */
@Data
@Schema(description = "Update folder request")
public class UpdateFolderRequest {

    @Schema(description = "Folder name", example = "Updated Folder Name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Folder name cannot be blank")
    private String name;

    @Schema(description = "Parent folder ID", example = "folder_root")
    private String parentId;
}
