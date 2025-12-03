package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.service.FileService;
import com.halolight.web.dto.file.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * File storage controller
 */
@Tag(name = "Files", description = "File storage management API endpoints")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload file", description = "Upload a new file (metadata only)")
    @PostMapping
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            Authentication authentication,
            @Valid @RequestBody UploadFileRequest request
    ) {
        String userId = getUserId(authentication);
        FileResponse file = fileService.uploadFile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", file));
    }

    @Operation(summary = "Get files", description = "Get paginated list of files and folders with filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FileResponse>>> getFiles(
            Authentication authentication,
            @Parameter(description = "File path filter") @RequestParam(required = false) String path,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "File type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search
    ) {
        String userId = getUserId(authentication);

        QueryFilesRequest request = new QueryFilesRequest();
        request.setPath(path);
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setType(type);
        request.setSearch(search);

        Page<FileResponse> files = fileService.getFiles(userId, request);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @Operation(summary = "Get file by ID", description = "Get file details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileById(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        FileResponse file = fileService.getFileById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(file));
    }

    @Operation(summary = "Get download URL", description = "Get temporary download URL for a file")
    @GetMapping("/{id}/download-url")
    public ResponseEntity<ApiResponse<FileService.DownloadUrlResponse>> getDownloadUrl(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        FileService.DownloadUrlResponse response = fileService.getDownloadUrl(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete file", description = "Delete a file")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        fileService.deleteFile(id, userId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    @Operation(summary = "Batch delete files", description = "Delete multiple files")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDeleteFiles(
            Authentication authentication,
            @RequestBody BatchDeleteRequest request
    ) {
        String userId = getUserId(authentication);
        fileService.batchDeleteFiles(request.getIds(), userId);
        return ResponseEntity.ok(ApiResponse.success("Files deleted successfully", null));
    }

    @Operation(summary = "Move file", description = "Move file to another folder")
    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<FileResponse>> moveFile(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody MoveFileRequest request
    ) {
        String userId = getUserId(authentication);
        // Extract folder ID from target path (simplified)
        String targetFolderId = extractFolderIdFromPath(request.getTargetPath());
        FileResponse file = fileService.moveFile(id, userId, targetFolderId);
        return ResponseEntity.ok(ApiResponse.success("File moved successfully", file));
    }

    @Operation(summary = "Rename file", description = "Rename a file")
    @PatchMapping("/{id}/rename")
    public ResponseEntity<ApiResponse<FileResponse>> renameFile(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody RenameFileRequest request
    ) {
        String userId = getUserId(authentication);
        FileResponse file = fileService.renameFile(id, userId, request.getName());
        return ResponseEntity.ok(ApiResponse.success("File renamed successfully", file));
    }

    @Operation(summary = "Copy file", description = "Copy file to another location")
    @PostMapping("/{id}/copy")
    public ResponseEntity<ApiResponse<FileResponse>> copyFile(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody Map<String, String> body
    ) {
        String userId = getUserId(authentication);
        String targetPath = body.get("targetPath");
        String targetFolderId = extractFolderIdFromPath(targetPath);
        FileResponse file = fileService.copyFile(id, userId, targetFolderId);
        return ResponseEntity.ok(ApiResponse.success("File copied successfully", file));
    }

    @Operation(summary = "Toggle favorite", description = "Toggle file favorite status")
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<FileResponse>> toggleFavorite(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody ToggleFavoriteRequest request
    ) {
        String userId = getUserId(authentication);
        FileResponse file = fileService.toggleFavorite(id, userId, request.getFavorite());
        return ResponseEntity.ok(ApiResponse.success("Favorite status updated", file));
    }

    @Operation(summary = "Share file", description = "Create a share link for a file")
    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<FileService.ShareLinkResponse>> shareFile(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody ShareFileRequest request
    ) {
        String userId = getUserId(authentication);
        FileService.ShareLinkResponse response = fileService.shareFile(
                id, userId, request.getExpiresIn(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Share link created", response));
    }

    @Operation(summary = "Get storage quota", description = "Get user storage quota and usage statistics")
    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<StorageStatsResponse>> getStorageQuota(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        StorageStatsResponse stats = fileService.getStorageQuota(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get storage info", description = "Get storage information (alias for /quota)")
    @GetMapping("/storage")
    public ResponseEntity<ApiResponse<StorageStatsResponse>> getStorageInfo(
            Authentication authentication
    ) {
        return getStorageQuota(authentication);
    }

    @Operation(summary = "Get storage info (alternative)", description = "Get storage information (alias for /quota)")
    @GetMapping("/storage-info")
    public ResponseEntity<ApiResponse<StorageStatsResponse>> getStorageInfoAlt(
            Authentication authentication
    ) {
        return getStorageQuota(authentication);
    }

    /**
     * Extract user ID from authentication
     */
    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return authentication.getName();
    }

    /**
     * Extract folder ID from path (simplified implementation)
     */
    private String extractFolderIdFromPath(String path) {
        // In a real implementation, you would look up the folder by path
        // For now, return null (root level)
        if (path == null || path.equals("/") || path.isEmpty()) {
            return null;
        }
        // This is a simplified approach - you might want to parse the path
        // and look up the actual folder ID
        return null;
    }
}
