package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.security.UserPrincipal;
import com.halolight.service.MessageService;
import com.halolight.web.dto.message.ConversationResponse;
import com.halolight.web.dto.message.CreateConversationRequest;
import com.halolight.web.dto.message.MessageResponse;
import com.halolight.web.dto.message.SendMessageRequest;
import com.halolight.web.dto.message.SendMessageWithConversationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Messages", description = "Conversation and message APIs")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "List conversations")
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> listConversations(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ConversationResponse> conversations = messageService.listConversations(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @Operation(summary = "Create conversation (direct or group)")
    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateConversationRequest request
    ) {
        ConversationResponse convo = messageService.createConversation(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Conversation created", convo));
    }

    @Operation(summary = "List messages in conversation")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> listMessages(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MessageResponse> messages = messageService.listMessages(user.getId(), conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "Send message")
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse message = messageService.sendMessage(user.getId(), conversationId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @Operation(summary = "Mark conversation as read")
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId
    ) {
        messageService.markConversationRead(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation marked as read", null));
    }

    // ==================== Next.js Compatible Endpoints ====================

    @Operation(summary = "Get messages (Next.js compatible)", description = "Alternative endpoint: GET /messages/{conversationId}")
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> listMessagesCompat(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MessageResponse> messages = messageService.listMessages(user.getId(), conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "Send message (Next.js compatible)", description = "Alternative endpoint: POST /messages/send with conversationId in body")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessageCompat(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody SendMessageWithConversationRequest request
    ) {
        SendMessageRequest sendRequest = new SendMessageRequest();
        sendRequest.setContent(request.getContent());
        sendRequest.setType(request.getType());
        MessageResponse message = messageService.sendMessage(user.getId(), request.getConversationId(), sendRequest);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @Operation(summary = "Mark conversation as read (Next.js compatible)", description = "Alternative endpoint: PUT /messages/{conversationId}/read")
    @PutMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markReadCompat(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId
    ) {
        messageService.markConversationRead(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation marked as read", null));
    }

    @Operation(summary = "Delete conversation (Next.js compatible)")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String conversationId
    ) {
        messageService.deleteConversation(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }
}
