package com.halolight.web.dto.document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTagsRequest {
    @NotNull
    private List<String> tags;
}
