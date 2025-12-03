package com.halolight.web.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameDocumentRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
}
