package com.halolight.service;

import com.halolight.domain.entity.Document;
import com.halolight.domain.entity.DocumentShare;
import com.halolight.domain.entity.DocumentTag;
import com.halolight.domain.entity.Tag;
import com.halolight.domain.entity.User;
import com.halolight.domain.entity.id.DocumentTagId;
import com.halolight.domain.repository.DocumentRepository;
import com.halolight.domain.repository.DocumentShareRepository;
import com.halolight.domain.repository.TagRepository;
import com.halolight.domain.repository.UserRepository;
import com.halolight.dto.UserDTO;
import com.halolight.dto.UserMapper;
import com.halolight.web.dto.document.CreateDocumentRequest;
import com.halolight.web.dto.document.DocumentResponse;
import com.halolight.web.dto.document.ShareDocumentRequest;
import com.halolight.web.dto.document.UnshareDocumentRequest;
import com.halolight.web.dto.document.UpdateDocumentRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing documents
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentShareRepository documentShareRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Get documents for a user with optional filtering
     *
     * @param userId   User ID
     * @param type     Optional document type filter
     * @param folder   Optional folder filter
     * @param search   Optional search query
     * @param pageable Pagination parameters
     * @return Page of document responses
     */
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocuments(String userId, String type, String folder, String search, Pageable pageable) {
        Page<Document> documents = documentRepository.findByOwnerIdAndFilters(userId, type, folder, search, pageable);
        return documents.map(this::convertToResponse);
    }

    /**
     * Get documents shared with a user
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of document responses
     */
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getSharedDocuments(String userId, Pageable pageable) {
        Page<Document> documents = documentRepository.findAccessibleByUserId(userId, pageable);
        return documents.map(this::convertToResponse);
    }

    /**
     * Get document by ID with permission check
     *
     * @param documentId Document ID
     * @param userId     Current user ID
     * @return Document response
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(String documentId, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Check if user has access to this document
        if (!hasAccess(document, userId)) {
            throw new AccessDeniedException("You do not have access to this document");
        }

        // Increment view count
        document.setViews(document.getViews() + 1);
        documentRepository.save(document);

        return convertToResponse(document);
    }

    /**
     * Create a new document
     *
     * @param request Create document request
     * @param ownerId Owner user ID
     * @return Created document response
     */
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + ownerId));

        Document document = Document.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .folder(request.getFolder() != null ? request.getFolder() : "/")
                .type(request.getType() != null ? request.getType() : "document")
                .size(BigInteger.valueOf(request.getContent().length()))
                .views(0)
                .ownerId(ownerId)
                .teamId(request.getTeamId())
                .build();

        Document savedDocument = documentRepository.save(document);

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            updateDocumentTags(savedDocument, request.getTags());
        }

        return convertToResponse(savedDocument);
    }

    /**
     * Update an existing document
     *
     * @param documentId Document ID
     * @param request    Update document request
     * @param userId     Current user ID
     * @return Updated document response
     */
    @Transactional
    public DocumentResponse updateDocument(String documentId, UpdateDocumentRequest request, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Check ownership or edit permission
        if (!hasEditPermission(document, userId)) {
            throw new AccessDeniedException("You do not have permission to edit this document");
        }

        // Update fields
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
            document.setSize(BigInteger.valueOf(request.getContent().length()));
        }
        if (request.getFolder() != null) {
            document.setFolder(request.getFolder());
        }
        if (request.getType() != null) {
            document.setType(request.getType());
        }

        Document updatedDocument = documentRepository.save(document);

        // Update tags if provided
        if (request.getTags() != null) {
            updateDocumentTags(updatedDocument, request.getTags());
        }

        return convertToResponse(updatedDocument);
    }

    /**
     * Delete a document (soft delete by removing shares and optionally marking as deleted)
     *
     * @param documentId Document ID
     * @param userId     Current user ID
     */
    @Transactional
    public void deleteDocument(String documentId, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Only owner can delete
        if (!document.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this document");
        }

        // Remove all shares
        documentShareRepository.deleteByDocumentId(documentId);

        // Delete the document
        documentRepository.delete(document);
    }

    /**
     * Share a document with users or teams
     *
     * @param documentId Document ID
     * @param request    Share document request
     * @param userId     Current user ID
     * @return Updated document response
     */
    @Transactional
    public DocumentResponse shareDocument(String documentId, ShareDocumentRequest request, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Only owner can share
        if (!document.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to share this document");
        }

        // Share with users
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String targetUserId : request.getUserIds()) {
                if (!documentShareRepository.existsByDocumentIdAndSharedWithId(documentId, targetUserId)) {
                    DocumentShare share = DocumentShare.builder()
                            .documentId(documentId)
                            .sharedWithId(targetUserId)
                            .permission(request.getPermission())
                            .expiresAt(request.getExpiresAt())
                            .build();
                    documentShareRepository.save(share);
                }
            }
        }

        // Share with teams
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            for (String teamId : request.getTeamIds()) {
                if (!documentShareRepository.existsByDocumentIdAndTeamId(documentId, teamId)) {
                    DocumentShare share = DocumentShare.builder()
                            .documentId(documentId)
                            .teamId(teamId)
                            .permission(request.getPermission())
                            .expiresAt(request.getExpiresAt())
                            .build();
                    documentShareRepository.save(share);
                }
            }
        }

        return convertToResponse(document);
    }

    /**
     * Unshare document from users or teams
     */
    @Transactional
    public DocumentResponse unshareDocument(String documentId, com.halolight.web.dto.document.UnshareDocumentRequest request, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        if (!document.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to unshare this document");
        }

        if (request.getUserIds() != null) {
            for (String targetUserId : request.getUserIds()) {
                documentShareRepository.findByDocumentIdAndSharedWithId(documentId, targetUserId)
                        .ifPresent(documentShareRepository::delete);
            }
        }
        if (request.getTeamIds() != null) {
            for (String teamId : request.getTeamIds()) {
                documentShareRepository.findByDocumentIdAndTeamId(documentId, teamId)
                        .ifPresent(documentShareRepository::delete);
            }
        }
        return convertToResponse(document);
    }

    /**
     * Rename a document.
     */
    @Transactional
    public DocumentResponse renameDocument(String documentId, String newTitle, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));
        if (!hasEditPermission(document, userId)) {
            throw new AccessDeniedException("You do not have permission to rename this document");
        }
        document.setTitle(newTitle);
        document = documentRepository.save(document);
        return convertToResponse(document);
    }

    /**
     * Move a document to a different folder.
     */
    @Transactional
    public DocumentResponse moveDocument(String documentId, String folder, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));
        if (!hasEditPermission(document, userId)) {
            throw new AccessDeniedException("You do not have permission to move this document");
        }
        document.setFolder(folder);
        document = documentRepository.save(document);
        return convertToResponse(document);
    }

    /**
     * Update tags for a document.
     */
    @Transactional
    public DocumentResponse updateTags(String documentId, List<String> tags, String userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));
        if (!hasEditPermission(document, userId)) {
            throw new AccessDeniedException("You do not have permission to update tags for this document");
        }
        updateDocumentTags(document, tags);
        return convertToResponse(document);
    }

    /**
     * Batch delete documents owned by the current user.
     */
    @Transactional
    public void batchDeleteDocuments(List<String> ids, String userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            Document document = documentRepository.findById(id).orElse(null);
            if (document != null && document.getOwnerId().equals(userId)) {
                documentShareRepository.deleteByDocumentId(id);
                documentRepository.delete(document);
            }
        }
    }

    /**
     * Update document tags
     *
     * @param document Document entity
     * @param tagNames List of tag names
     */
    private void updateDocumentTags(Document document, List<String> tagNames) {
        // Remove existing tags
        document.getTags().clear();

        // Add new tags
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .build();
                        return tagRepository.save(newTag);
                    });

            DocumentTagId documentTagId = new DocumentTagId();
            documentTagId.setDocumentId(document.getId());
            documentTagId.setTagId(tag.getId());

            DocumentTag documentTag = DocumentTag.builder()
                    .id(documentTagId)
                    .document(document)
                    .tag(tag)
                    .build();

            document.getTags().add(documentTag);
        }

        documentRepository.save(document);
    }

    /**
     * Convert Document entity to DocumentResponse DTO
     *
     * @param document Document entity
     * @return Document response DTO
     */
    private DocumentResponse convertToResponse(Document document) {
        // Get tags
        List<String> tags = document.getTags().stream()
                .map(dt -> dt.getTag().getName())
                .collect(Collectors.toList());

        // Get collaborators (users who have access via shares)
        List<DocumentShare> shares = documentShareRepository.findByDocumentId(document.getId());
        List<UserDTO> collaborators = shares.stream()
                .filter(share -> share.getSharedWithId() != null)
                .map(share -> userRepository.findById(share.getSharedWithId()).orElse(null))
                .filter(user -> user != null)
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        // Get owner info
        UserDTO owner = userRepository.findById(document.getOwnerId())
                .map(userMapper::toDTO)
                .orElse(null);

        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .folder(document.getFolder())
                .type(document.getType())
                .size(document.getSize())
                .views(document.getViews())
                .ownerId(document.getOwnerId())
                .owner(owner)
                .teamId(document.getTeamId())
                .shared(!shares.isEmpty())
                .tags(tags)
                .collaborators(collaborators)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Check if user has access to a document
     *
     * @param document Document entity
     * @param userId   User ID
     * @return true if user has access, false otherwise
     */
    private boolean hasAccess(Document document, String userId) {
        // Owner always has access
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        // Check if document is shared with user
        if (documentShareRepository.existsByDocumentIdAndSharedWithId(document.getId(), userId)) {
            return true;
        }

        // Check if document is shared with user's teams
        // TODO: Implement team membership check when TeamMember repository is available
        // For now, assuming team sharing is handled separately

        return false;
    }

    /**
     * Check if user has edit permission for a document
     *
     * @param document Document entity
     * @param userId   User ID
     * @return true if user can edit, false otherwise
     */
    private boolean hasEditPermission(Document document, String userId) {
        // Owner always has edit permission
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        // Check if user has EDIT permission via share
        return documentShareRepository.findByDocumentIdAndSharedWithId(document.getId(), userId)
                .map(share -> share.getPermission() == com.halolight.domain.entity.enums.SharePermission.EDIT)
                .orElse(false);
    }
}
