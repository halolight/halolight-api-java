package com.halolight.web.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class ConversationResponse {
    private String id;
    private String name;
    private boolean group;
    private String teamId;
    private Set<String> participantIds;
    private Instant createdAt;
    private Instant updatedAt;
    private MessageResponse lastMessage;
}
