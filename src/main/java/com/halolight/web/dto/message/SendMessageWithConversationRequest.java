package com.halolight.web.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for Next.js compatible /messages/send endpoint.
 * Includes conversationId in the request body.
 */
@Data
public class SendMessageWithConversationRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Content is required")
    private String content;

    private String type = "text";
}
