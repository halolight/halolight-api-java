package com.halolight.web.dto.document;

import com.halolight.domain.entity.enums.SharePermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Request DTO for sharing a document with users or teams
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to share a document")
public class ShareDocumentRequest {

    @Schema(description = "User IDs to share the document with", example = "[\"user_abc123\", \"user_def456\"]")
    private List<String> userIds;

    @Schema(description = "Team IDs to share the document with", example = "[\"team_abc123\"]")
    private List<String> teamIds;

    @NotNull(message = "Permission is required")
    @Schema(description = "Permission level for shared users/teams", example = "READ", required = true, defaultValue = "READ")
    @Builder.Default
    private SharePermission permission = SharePermission.READ;

    @Schema(description = "Expiration timestamp for the share (optional)", example = "2025-12-31T23:59:59Z")
    private Instant expiresAt;
}
