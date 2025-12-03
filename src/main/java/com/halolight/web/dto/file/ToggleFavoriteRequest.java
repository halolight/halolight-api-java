package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Toggle favorite request DTO
 */
@Data
@Schema(description = "Toggle favorite request")
public class ToggleFavoriteRequest {

    @Schema(description = "Favorite status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Favorite status cannot be null")
    private Boolean favorite;
}
