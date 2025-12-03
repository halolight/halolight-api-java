package com.halolight.web.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MoveDocumentRequest {
    @NotBlank
    @Size(max = 255)
    private String folder;
}
