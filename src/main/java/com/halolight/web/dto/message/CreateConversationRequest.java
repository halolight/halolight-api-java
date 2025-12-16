package com.halolight.web.dto.message;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateConversationRequest {
    private String name;
    private boolean group;
    private String teamId;

    @NotEmpty
    private List<String> participantIds;
}
