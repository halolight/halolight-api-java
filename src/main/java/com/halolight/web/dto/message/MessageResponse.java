package com.halolight.web.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private String type;
    private Boolean edited;
    private Instant createdAt;
    private Instant updatedAt;
}
