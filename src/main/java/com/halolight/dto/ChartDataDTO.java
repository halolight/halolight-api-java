package com.halolight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Chart data transfer object for dashboard visualizations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO implements Serializable {

    private List<String> labels;
    private List<DatasetDTO> datasets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetDTO implements Serializable {
        private String label;
        private List<Long> data;
        private String backgroundColor;
        private String borderColor;
    }
}
