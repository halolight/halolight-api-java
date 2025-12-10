package com.halolight.service;

import com.halolight.domain.entity.Conversation;
import com.halolight.domain.entity.ConversationParticipant;
import com.halolight.domain.entity.Message;
import com.halolight.domain.entity.User;
import com.halolight.domain.entity.id.ConversationParticipantId;
import com.halolight.domain.repository.ConversationParticipantRepository;
import com.halolight.domain.repository.ConversationRepository;
import com.halolight.domain.repository.MessageRepository;
import com.halolight.domain.repository.UserRepository;
import com.halolight.web.dto.message.ConversationResponse;
import com.halolight.web.dto.message.CreateConversationRequest;
import com.halolight.web.dto.message.MessageResponse;
import com.halolight.web.dto.message.SendMessageRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ConversationResponse> listConversations(String userId, Pageable pageable) {
        return conversationRepository.findByParticipantUserId(userId, pageable)
                .map(convo -> ConversationResponse.builder()
                        .id(convo.getId())
                        .name(convo.getName())
                        .group(Boolean.TRUE.equals(convo.getIsGroup()))
                        .teamId(convo.getTeamId())
                        .participantIds(convo.getParticipants().stream()
                                .map(p -> p.getUser().getId())
                                .collect(Collectors.toSet()))
                        .createdAt(convo.getCreatedAt())
                        .updatedAt(convo.getUpdatedAt())
                        .lastMessage(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(convo.getId())
                                .map(this::toMessageResponse)
                                .orElse(null))
                        .build());
    }

    @Transactional
    public ConversationResponse createConversation(String initiatorId, CreateConversationRequest request) {
        Set<String> participants = new HashSet<>(request.getParticipantIds());
        participants.add(initiatorId);

        // for direct chat, attempt reuse existing conversation
        if (!request.isGroup() && participants.size() == 2) {
            var ids = participants.toArray(new String[0]);
            var existing = conversationRepository.findDirectConversation(ids[0], ids[1]);
            if (existing.isPresent()) {
                Conversation convo = existing.get();
                return toConversationResponse(convo);
            }
        }

        Conversation convo = Conversation.builder()
                .name(request.getName())
                .isGroup(request.isGroup())
                .teamId(request.getTeamId())
                .build();
        convo = conversationRepository.save(convo);

        for (String pid : participants) {
            User user = userRepository.findById(pid)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + pid));
            ConversationParticipant cp = ConversationParticipant.builder()
                    .id(new ConversationParticipantId(convo.getId(), pid))
                    .conversation(convo)
                    .user(user)
                    .joinedAt(Instant.now())
                    .build();
            participantRepository.save(cp);
        }
        return toConversationResponse(convo);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(String userId, String conversationId, Pageable pageable) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        ensureParticipant(convo, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::toMessageResponse);
    }

    @Transactional
    public MessageResponse sendMessage(String userId, String conversationId, SendMessageRequest request) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        ensureParticipant(convo, userId);

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(userId)
                .content(request.getContent())
                .type(request.getType())
                .build();
        message = messageRepository.save(message);

        // update last_read? here only update conversation updatedAt via save
        convo.setUpdatedAt(Instant.now());
        conversationRepository.save(convo);

        return toMessageResponse(message);
    }

    @Transactional
    public void markConversationRead(String userId, String conversationId) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        ConversationParticipant participant = participantRepository.findById(new ConversationParticipantId(conversationId, userId))
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));
        participant.setLastReadAt(Instant.now());
        participantRepository.save(participant);
    }

    @Transactional
    public void deleteConversation(String userId, String conversationId) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        ensureParticipant(convo, userId);

        // Delete all messages in the conversation
        messageRepository.deleteByConversationId(conversationId);

        // Delete all participants
        participantRepository.deleteByIdConversationId(conversationId);

        // Delete the conversation itself
        conversationRepository.delete(convo);
    }

    private void ensureParticipant(Conversation convo, String userId) {
        boolean isParticipant = convo.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(userId));
        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant of this conversation");
        }
    }

    private ConversationResponse toConversationResponse(Conversation convo) {
        return ConversationResponse.builder()
                .id(convo.getId())
                .name(convo.getName())
                .group(Boolean.TRUE.equals(convo.getIsGroup()))
                .teamId(convo.getTeamId())
                .participantIds(convo.getParticipants().stream()
                        .map(p -> p.getUser().getId())
                        .collect(Collectors.toSet()))
                .createdAt(convo.getCreatedAt())
                .updatedAt(convo.getUpdatedAt())
                .lastMessage(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(convo.getId())
                        .map(this::toMessageResponse)
                        .orElse(null))
                .build();
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .type(message.getType())
                .edited(message.getIsEdited())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
