package com.halolight.web.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new document")
public class CreateDocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Document title", example = "HaloLight API 使用指南", required = true)
    private String title;

    @NotBlank(message = "Content is required")
    @Schema(description = "Document content in Markdown format", example = "# HaloLight API 使用指南\n\n## 概述\n\n...", required = true)
    private String content;

    @Size(max = 255, message = "Folder path must not exceed 255 characters")
    @Schema(description = "Folder path where document will be stored", example = "/documents")
    private String folder;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Schema(description = "Document type", example = "document", defaultValue = "document")
    private String type;

    @Schema(description = "Team ID if document belongs to a team", example = "team_abc123")
    private String teamId;

    @Schema(description = "Tags to associate with the document", example = "[\"技术文档\", \"重要\"]")
    private List<String> tags;
}
