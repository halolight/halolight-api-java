package com.halolight.web.dto.folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Folder response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Folder response")
public class FolderResponse {

    @Schema(description = "Folder ID", example = "folder_123")
    private String id;

    @Schema(description = "Folder name", example = "Design Docs")
    private String name;

    @Schema(description = "Parent folder ID", example = "folder_root")
    private String parentId;

    @Schema(description = "Number of files in folder", example = "5")
    private Long fileCount;

    @Schema(description = "Number of subfolders", example = "2")
    private Long childCount;

    @Schema(description = "Creation timestamp", example = "2024-01-10T00:00:00Z")
    private String createdAt;

    @Schema(description = "Update timestamp", example = "2024-01-15T10:30:00Z")
    private String updatedAt;
}
