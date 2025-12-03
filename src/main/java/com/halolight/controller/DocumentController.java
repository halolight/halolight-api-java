package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.security.UserPrincipal;
import com.halolight.service.DocumentService;
import com.halolight.dto.BatchDeleteRequest;
import com.halolight.web.dto.document.CreateDocumentRequest;
import com.halolight.web.dto.document.DocumentResponse;
import com.halolight.web.dto.document.MoveDocumentRequest;
import com.halolight.web.dto.document.RenameDocumentRequest;
import com.halolight.web.dto.document.ShareDocumentRequest;
import com.halolight.web.dto.document.UnshareDocumentRequest;
import com.halolight.web.dto.document.UpdateDocumentRequest;
import com.halolight.web.dto.document.UpdateTagsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for document management
 */
@Tag(name = "Documents", description = "Document management API endpoints")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Get list of documents with optional filtering
     *
     * @param type          Optional document type filter
     * @param folder        Optional folder filter
     * @param search        Optional search query
     * @param pageable      Pagination parameters
     * @param userPrincipal Authenticated user principal
     * @return Page of document responses
     */
    @Operation(
            summary = "List documents",
            description = "Retrieve a paginated list of documents owned by the authenticated user with optional filtering by type, folder, and search query"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocuments(
            @Parameter(description = "Document type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Folder path filter") @RequestParam(required = false) String folder,
            @Parameter(description = "Search query for title and content") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<DocumentResponse> documents = documentService.getUserDocuments(
                userPrincipal.getId(),
                type,
                folder,
                search,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * Get documents shared with the authenticated user
     *
     * @param pageable      Pagination parameters
     * @param userPrincipal Authenticated user principal
     * @return Page of shared document responses
     */
    @Operation(
            summary = "Get shared documents",
            description = "Retrieve a paginated list of documents that have been shared with the authenticated user"
    )
    @GetMapping("/shared")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getSharedDocuments(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<DocumentResponse> documents = documentService.getSharedDocuments(
                userPrincipal.getId(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * Get a specific document by ID
     *
     * @param id            Document ID
     * @param userPrincipal Authenticated user principal
     * @return Document response
     */
    @Operation(
            summary = "Get document detail",
            description = "Retrieve detailed information about a specific document by its ID. User must have access to the document."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        DocumentResponse document = documentService.getDocument(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(document));
    }

    /**
     * Create a new document
     *
     * @param request       Create document request
     * @param userPrincipal Authenticated user principal
     * @return Created document response
     */
    @Operation(
            summary = "Create document",
            description = "Create a new document owned by the authenticated user"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        DocumentResponse document = documentService.createDocument(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document created successfully", document));
    }

    /**
     * Update an existing document
     *
     * @param id            Document ID
     * @param request       Update document request
     * @param userPrincipal Authenticated user principal
     * @return Updated document response
     */
    @Operation(
            summary = "Update document",
            description = "Update an existing document. User must be the owner or have edit permission."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        DocumentResponse document = documentService.updateDocument(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", document));
    }

    /**
     * Delete a document
     *
     * @param id            Document ID
     * @param userPrincipal Authenticated user principal
     * @return Success response
     */
    @Operation(
            summary = "Delete document",
            description = "Delete a document. Only the owner can delete a document."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        documentService.deleteDocument(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    /**
     * Share a document with users or teams
     *
     * @param id            Document ID
     * @param request       Share document request
     * @param userPrincipal Authenticated user principal
     * @return Updated document response
     */
    @Operation(
            summary = "Share document",
            description = "Share a document with specified users or teams. Only the owner can share a document."
    )
    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<DocumentResponse>> shareDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable String id,
            @Valid @RequestBody ShareDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        DocumentResponse document = documentService.shareDocument(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document shared successfully", document));
    }

    @Operation(summary = "Unshare document", description = "Remove sharing for specified users or teams")
    @PostMapping("/{id}/unshare")
    public ResponseEntity<ApiResponse<DocumentResponse>> unshareDocument(
            @PathVariable String id,
            @Valid @RequestBody UnshareDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DocumentResponse document = documentService.unshareDocument(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document unshared successfully", document));
    }

    @Operation(summary = "Rename document", description = "Rename a document")
    @PatchMapping("/{id}/rename")
    public ResponseEntity<ApiResponse<DocumentResponse>> renameDocument(
            @PathVariable String id,
            @Valid @RequestBody RenameDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DocumentResponse document = documentService.renameDocument(id, request.getTitle(), userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document renamed successfully", document));
    }

    @Operation(summary = "Move document", description = "Move document to a different folder")
    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<DocumentResponse>> moveDocument(
            @PathVariable String id,
            @Valid @RequestBody MoveDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DocumentResponse document = documentService.moveDocument(id, request.getFolder(), userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document moved successfully", document));
    }

    @Operation(summary = "Update document tags", description = "Replace tags for a document")
    @PostMapping("/{id}/tags")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateTags(
            @PathVariable String id,
            @Valid @RequestBody UpdateTagsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DocumentResponse document = documentService.updateTags(id, request.getTags(), userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Document tags updated successfully", document));
    }

    @Operation(summary = "Batch delete documents", description = "Delete multiple documents by IDs")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDeleteDocuments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody com.halolight.dto.BatchDeleteRequest request
    ) {
        documentService.batchDeleteDocuments(request.getIds(), userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Documents deleted successfully", null));
    }
}
