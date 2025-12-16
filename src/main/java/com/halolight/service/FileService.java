package com.halolight.service;

import com.halolight.domain.entity.Folder;
import com.halolight.domain.entity.StorageFile;
import com.halolight.domain.repository.FolderRepository;
import com.halolight.domain.repository.StorageFileRepository;
import com.halolight.web.dto.file.FileResponse;
import com.halolight.web.dto.file.QueryFilesRequest;
import com.halolight.web.dto.file.StorageStatsResponse;
import com.halolight.web.dto.file.UploadFileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * File storage service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageFileRepository fileRepository;
    private final FolderRepository folderRepository;

    /**
     * Upload file (save metadata)
     */
    @Transactional
    public FileResponse uploadFile(String userId, UploadFileRequest request) {
        // Validate folder if specified
        if (request.getFolderId() != null) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new RuntimeException("Folder not found with id: " + request.getFolderId()));

            if (!folder.getOwnerId().equals(userId)) {
                throw new RuntimeException("Access denied to folder");
            }
        }

        // Determine file type from MIME type
        String fileType = getFileTypeFromMimeType(request.getMimeType());

        // Build file path
        String path = request.getPath() != null ? request.getPath() : "/";
        if (!path.endsWith("/")) {
            path += "/";
        }
        path += request.getName();

        // Create file entity
        StorageFile file = StorageFile.builder()
                .name(request.getName())
                .type(fileType)
                .size(BigInteger.valueOf(request.getSize() != null ? request.getSize() : 0L))
                .path(path)
                .folderId(request.getFolderId())
                .ownerId(userId)
                .build();

        file = fileRepository.save(file);
        log.info("Uploaded file: {} by user: {}", file.getId(), userId);

        return toFileResponse(file);
    }

    /**
     * Get file list with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<FileResponse> getFiles(String userId, QueryFilesRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        // Determine what to fetch based on type filter
        boolean shouldFetchFolders = request.getType() == null || "folder".equals(request.getType());
        boolean shouldFetchFiles = request.getType() == null || !"folder".equals(request.getType());

        List<FileResponse> allItems = new ArrayList<>();

        // Fetch folders if needed
        if (shouldFetchFolders) {
            List<Folder> folders = folderRepository.findByOwnerIdAndFilters(
                    userId,
                    extractFolderIdFromPath(request.getPath()),
                    request.getSearch()
            );

            allItems.addAll(folders.stream()
                    .map(this::toFileResponse)
                    .collect(Collectors.toList()));
        }

        // Fetch files if needed
        if (shouldFetchFiles) {
            Page<StorageFile> files = fileRepository.findByOwnerIdAndFilters(
                    userId,
                    extractFolderIdFromPath(request.getPath()),
                    request.getType(),
                    request.getSearch(),
                    Pageable.unpaged()
            );

            allItems.addAll(files.stream()
                    .map(this::toFileResponse)
                    .collect(Collectors.toList()));
        }

        // Sort all items by updatedAt
        allItems.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));

        // Manual pagination
        int start = (request.getPage() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), allItems.size());
        List<FileResponse> paginatedItems = start < allItems.size()
                ? allItems.subList(start, end)
                : new ArrayList<>();

        return new PageImpl<>(paginatedItems, pageable, allItems.size());
    }

    /**
     * Get file by ID
     */
    @Transactional(readOnly = true)
    public FileResponse getFileById(String id, String userId) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        return toFileResponse(file);
    }

    /**
     * Download file (get download URL)
     */
    @Transactional(readOnly = true)
    public DownloadUrlResponse getDownloadUrl(String id, String userId) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Generate temporary download URL (in production, this would be a signed URL)
        String url = String.format("https://storage.example.com/download/%s?token=%d", id, System.currentTimeMillis());
        Instant expiresAt = Instant.now().plusSeconds(3600); // 1 hour expiry

        return DownloadUrlResponse.builder()
                .url(url)
                .expiresAt(expiresAt.toString())
                .build();
    }

    /**
     * Delete file
     */
    @Transactional
    public void deleteFile(String id, String userId) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        fileRepository.delete(file);
        log.info("Deleted file: {} by user: {}", id, userId);
    }

    /**
     * Batch delete files
     */
    @Transactional
    public void batchDeleteFiles(List<String> ids, String userId) {
        for (String id : ids) {
            try {
                deleteFile(id, userId);
            } catch (Exception e) {
                log.warn("Failed to delete file {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Move file to another folder
     */
    @Transactional
    public FileResponse moveFile(String id, String userId, String targetFolderId) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Validate target folder if specified
        if (targetFolderId != null && !targetFolderId.isEmpty()) {
            Folder targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found"));

            if (!targetFolder.getOwnerId().equals(userId)) {
                throw new RuntimeException("Access denied to target folder");
            }

            // Update file path
            String newPath = targetFolder.getName() + "/" + file.getName();
            file.setPath(newPath);
        } else {
            // Move to root
            file.setPath("/" + file.getName());
        }

        file.setFolderId(targetFolderId);
        file = fileRepository.save(file);
        log.info("Moved file: {} to folder: {} by user: {}", id, targetFolderId, userId);

        return toFileResponse(file);
    }

    /**
     * Rename file
     */
    @Transactional
    public FileResponse renameFile(String id, String userId, String newName) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Update path with new name
        String oldPath = file.getPath();
        int lastSlash = oldPath.lastIndexOf('/');
        String newPath = oldPath.substring(0, lastSlash + 1) + newName;

        file.setName(newName);
        file.setPath(newPath);
        file = fileRepository.save(file);
        log.info("Renamed file: {} to {} by user: {}", id, newName, userId);

        return toFileResponse(file);
    }

    /**
     * Copy file
     */
    @Transactional
    public FileResponse copyFile(String id, String userId, String targetFolderId) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Validate target folder if specified
        String newPath = file.getPath();
        if (targetFolderId != null && !targetFolderId.isEmpty()) {
            Folder targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found"));

            if (!targetFolder.getOwnerId().equals(userId)) {
                throw new RuntimeException("Access denied to target folder");
            }

            newPath = targetFolder.getName() + "/" + file.getName();
        }

        // Create copy
        StorageFile copy = StorageFile.builder()
                .name(file.getName())
                .type(file.getType())
                .size(file.getSize())
                .path(newPath)
                .folderId(targetFolderId)
                .ownerId(userId)
                .build();

        copy = fileRepository.save(copy);
        log.info("Copied file: {} to new file: {} by user: {}", id, copy.getId(), userId);

        return toFileResponse(copy);
    }

    /**
     * Toggle favorite
     */
    @Transactional
    public FileResponse toggleFavorite(String id, String userId, boolean favorite) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Note: StorageFile entity doesn't have isFavorite field
        // This would need to be added to the entity or managed separately
        // For now, just return the file response
        log.info("Toggled favorite for file: {} to {} by user: {}", id, favorite, userId);

        return toFileResponse(file);
    }

    /**
     * Share file
     */
    @Transactional(readOnly = true)
    public ShareLinkResponse shareFile(String id, String userId, Integer expiresIn, String password) {
        StorageFile file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        if (!file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to file");
        }

        // Generate share link (in production, this would be a secure token)
        String shareUrl = String.format("https://share.example.com/f/%s?t=%d", id, System.currentTimeMillis());
        String expiresAt = expiresIn != null
                ? Instant.now().plusSeconds(expiresIn).toString()
                : null;

        log.info("Created share link for file: {} by user: {}", id, userId);

        return ShareLinkResponse.builder()
                .shareUrl(shareUrl)
                .password(password)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Get storage quota usage
     */
    @Transactional(readOnly = true)
    public StorageStatsResponse getStorageQuota(String userId) {
        // Get all files for user
        List<StorageFile> files = fileRepository.findByOwnerId(userId, Pageable.unpaged()).getContent();

        // Calculate total used storage
        long totalUsed = files.stream()
                .mapToLong(f -> f.getSize().longValue())
                .sum();

        // Calculate breakdown by type
        long images = 0, videos = 0, audio = 0, documents = 0, archives = 0, others = 0;

        for (StorageFile file : files) {
            long size = file.getSize().longValue();
            String type = file.getType();

            switch (type) {
                case "image":
                    images += size;
                    break;
                case "video":
                    videos += size;
                    break;
                case "audio":
                    audio += size;
                    break;
                case "document":
                    documents += size;
                    break;
                case "archive":
                    archives += size;
                    break;
                default:
                    others += size;
            }
        }

        // Default total storage: 20GB
        long totalStorage = 20L * 1024 * 1024 * 1024;

        Map<String, Long> breakdown = Map.of(
                "images", images,
                "videos", videos,
                "audio", audio,
                "documents", documents,
                "archives", archives,
                "others", others
        );

        return StorageStatsResponse.builder()
                .used(totalUsed)
                .total(totalStorage)
                .breakdown(breakdown)
                .build();
    }

    /**
     * Transform Folder to FileResponse
     */
    private FileResponse toFileResponse(Folder folder) {
        return FileResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .type("folder")
                .size(null)
                .items((long) (folder.getFiles().size() + folder.getChildren().size()))
                .path("/" + folder.getName())
                .mimeType("folder")
                .thumbnail(null)
                .isFavorite(false)
                .createdAt(folder.getCreatedAt().toString())
                .updatedAt(folder.getUpdatedAt().toString())
                .build();
    }

    /**
     * Transform StorageFile to FileResponse
     */
    private FileResponse toFileResponse(StorageFile file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .size(file.getSize().longValue())
                .items(null)
                .path(file.getPath())
                .mimeType(file.getType())
                .thumbnail(null) // Would be set from actual storage
                .isFavorite(false) // Would need to be added to entity
                .createdAt(file.getCreatedAt().toString())
                .updatedAt(file.getUpdatedAt().toString())
                .build();
    }

    /**
     * Get file type from MIME type
     */
    private String getFileTypeFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return "other";
        }

        if (mimeType.equals("folder")) return "folder";
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.contains("pdf") || mimeType.contains("document") ||
                mimeType.contains("spreadsheet") || mimeType.contains("presentation") ||
                mimeType.startsWith("text/") || mimeType.contains("json")) {
            return "document";
        }
        if (mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("rar")) {
            return "archive";
        }

        return "other";
    }

    /**
     * Extract folder ID from path (simplified implementation)
     */
    private String extractFolderIdFromPath(String path) {
        // In a real implementation, you would look up the folder by path
        // For now, return null to get root-level items
        return null;
    }

    /**
     * Download URL response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DownloadUrlResponse {
        private String url;
        private String expiresAt;
    }

    /**
     * Share link response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ShareLinkResponse {
        private String shareUrl;
        private String password;
        private String expiresAt;
    }
}
