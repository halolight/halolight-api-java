package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Share file request DTO
 */
@Data
@Schema(description = "Share file request")
public class ShareFileRequest {

    @Schema(description = "Expiration time in seconds", example = "3600")
    private Integer expiresIn;

    @Schema(description = "Password protection", example = "secret123")
    private String password;
}
