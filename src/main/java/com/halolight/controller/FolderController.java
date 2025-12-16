package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.security.UserPrincipal;
import com.halolight.service.FolderService;
import com.halolight.web.dto.folder.CreateFolderRequest;
import com.halolight.web.dto.folder.FolderResponse;
import com.halolight.web.dto.folder.UpdateFolderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Folder management controller
 */
@Tag(name = "Folders", description = "Folder management API endpoints")
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "Create folder", description = "Create a new folder")
    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateFolderRequest request
    ) {
        FolderResponse folder = folderService.createFolder(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Folder created successfully", folder));
    }

    @Operation(summary = "Get folders", description = "Get list of folders with optional parent filter")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolders(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "Parent folder ID") @RequestParam(required = false) String parentId
    ) {
        List<FolderResponse> folders = folderService.getFolders(user.getId(), parentId);
        return ResponseEntity.ok(ApiResponse.success(folders));
    }

    @Operation(summary = "Get folder by ID", description = "Get folder details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        FolderResponse folder = folderService.getFolderById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(folder));
    }

    @Operation(summary = "Update folder", description = "Update folder name and/or parent")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateFolderRequest request
    ) {
        FolderResponse folder = folderService.updateFolder(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Folder updated successfully", folder));
    }

    @Operation(summary = "Rename folder", description = "Rename a folder")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> renameFolder(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, String> body
    ) {
        String newName = body.get("name");

        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Folder name cannot be blank"));
        }

        FolderResponse folder = folderService.renameFolder(id, user.getId(), newName);
        return ResponseEntity.ok(ApiResponse.success("Folder renamed successfully", folder));
    }

    @Operation(summary = "Delete folder", description = "Delete a folder (must be empty)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        folderService.deleteFolder(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Folder deleted successfully", null));
    }

    @Operation(summary = "Get folder tree", description = "Get hierarchical folder tree structure")
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<FolderService.FolderTreeNode>>> getFolderTree(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        List<FolderService.FolderTreeNode> tree = folderService.getFolderTree(user.getId());
        return ResponseEntity.ok(ApiResponse.success(tree));
    }
}
