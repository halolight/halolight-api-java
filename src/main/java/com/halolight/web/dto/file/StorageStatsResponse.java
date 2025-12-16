package com.halolight.web.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Storage statistics response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Storage statistics response")
public class StorageStatsResponse {

    @Schema(description = "Used storage in bytes", example = "5368709120")
    private Long used;

    @Schema(description = "Total storage in bytes", example = "21474836480")
    private Long total;

    @Schema(description = "Storage breakdown by type", example = "{\"images\": 1610612736, \"videos\": 2147483648}")
    private Map<String, Long> breakdown;
}
