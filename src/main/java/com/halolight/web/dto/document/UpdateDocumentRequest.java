package com.halolight.web.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating an existing document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an existing document")
public class UpdateDocumentRequest {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Document title", example = "HaloLight API 使用指南 v2.0")
    private String title;

    @Schema(description = "Document content in Markdown format", example = "# Updated Content\n\n...")
    private String content;

    @Size(max = 255, message = "Folder path must not exceed 255 characters")
    @Schema(description = "Folder path where document will be stored", example = "/documents/archives")
    private String folder;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Schema(description = "Document type", example = "document")
    private String type;

    @Schema(description = "Tags to associate with the document", example = "[\"技术文档\", \"重要\", \"已更新\"]")
    private List<String> tags;
}
