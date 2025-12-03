package com.halolight.web.dto.document;

import com.halolight.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for document data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Document response")
public class DocumentResponse {

    @Schema(description = "Document ID", example = "doc_abc123def456")
    private String id;

    @Schema(description = "Document title", example = "HaloLight API 使用指南")
    private String title;

    @Schema(description = "Document content in Markdown format", example = "# HaloLight API 使用指南\n\n...")
    private String content;

    @Schema(description = "Folder path", example = "/documents")
    private String folder;

    @Schema(description = "Document type", example = "document")
    private String type;

    @Schema(description = "Document size in bytes", example = "2048")
    private BigInteger size;

    @Schema(description = "View count", example = "128")
    private Integer views;

    @Schema(description = "Owner ID", example = "user_abc123")
    private String ownerId;

    @Schema(description = "Owner information")
    private UserDTO owner;

    @Schema(description = "Team ID if document belongs to a team", example = "team_abc123")
    private String teamId;

    @Schema(description = "Whether the document is shared", example = "true")
    private Boolean shared;

    @Schema(description = "Tags associated with the document", example = "[\"技术文档\", \"重要\"]")
    private List<String> tags;

    @Schema(description = "Collaborators who have access to this document")
    private List<UserDTO> collaborators;

    @Schema(description = "Creation timestamp", example = "2024-01-10T00:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}
