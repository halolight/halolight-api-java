package com.halolight.service;

import com.halolight.domain.entity.Folder;
import com.halolight.domain.entity.StorageFile;
import com.halolight.domain.repository.FolderRepository;
import com.halolight.domain.repository.StorageFileRepository;
import com.halolight.web.dto.folder.CreateFolderRequest;
import com.halolight.web.dto.folder.FolderResponse;
import com.halolight.web.dto.folder.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Folder management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final StorageFileRepository fileRepository;

    /**
     * Create folder
     */
    @Transactional
    public FolderResponse createFolder(String userId, CreateFolderRequest request) {
        // Check if folder with same name already exists in the same parent
        if (folderRepository.existsByOwnerIdAndNameAndParentId(userId, request.getName(), request.getParentId())) {
            throw new RuntimeException("Folder with this name already exists in the parent folder");
        }

        // Validate parent folder if specified
        if (request.getParentId() != null) {
            Folder parent = folderRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent folder not found with id: " + request.getParentId()));

            if (!parent.getOwnerId().equals(userId)) {
                throw new RuntimeException("Access denied to parent folder");
            }
        }

        // Create folder
        Folder folder = Folder.builder()
                .name(request.getName())
                .parentId(request.getParentId())
                .ownerId(userId)
                .build();

        folder = folderRepository.save(folder);
        log.info("Created folder: {} by user: {}", folder.getId(), userId);

        return toFolderResponse(folder);
    }

    /**
     * Get all folders for user
     */
    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(String userId, String parentId) {
        List<Folder> folders;

        if (parentId != null) {
            folders = folderRepository.findByParentId(parentId);
        } else {
            folders = folderRepository.findByOwnerIdAndParentIdIsNull(userId);
        }

        return folders.stream()
                .filter(f -> f.getOwnerId().equals(userId))
                .map(this::toFolderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get folder by ID
     */
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(String id, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        if (!folder.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to folder");
        }

        return toFolderResponse(folder);
    }

    /**
     * Update folder
     */
    @Transactional
    public FolderResponse updateFolder(String id, String userId, UpdateFolderRequest request) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        if (!folder.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to folder");
        }

        // Check if new name conflicts with siblings
        if (!folder.getName().equals(request.getName())) {
            if (folderRepository.existsByOwnerIdAndNameAndParentId(
                    userId, request.getName(), request.getParentId() != null ? request.getParentId() : folder.getParentId())) {
                throw new RuntimeException("Folder with this name already exists in the parent folder");
            }
        }

        // Update folder
        folder.setName(request.getName());
        if (request.getParentId() != null) {
            // Validate new parent
            if (!request.getParentId().equals(folder.getParentId())) {
                Folder newParent = folderRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));

                if (!newParent.getOwnerId().equals(userId)) {
                    throw new RuntimeException("Access denied to parent folder");
                }

                // Prevent circular reference
                if (isDescendantOf(newParent, id)) {
                    throw new RuntimeException("Cannot move folder to its own descendant");
                }

                folder.setParentId(request.getParentId());
            }
        }

        folder = folderRepository.save(folder);
        log.info("Updated folder: {} by user: {}", id, userId);

        return toFolderResponse(folder);
    }

    /**
     * Rename folder
     */
    @Transactional
    public FolderResponse renameFolder(String id, String userId, String newName) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        if (!folder.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to folder");
        }

        // Check if new name conflicts
        if (folderRepository.existsByOwnerIdAndNameAndParentId(userId, newName, folder.getParentId())) {
            throw new RuntimeException("Folder with this name already exists in the parent folder");
        }

        folder.setName(newName);
        folder = folderRepository.save(folder);
        log.info("Renamed folder: {} to {} by user: {}", id, newName, userId);

        return toFolderResponse(folder);
    }

    /**
     * Delete folder
     */
    @Transactional
    public void deleteFolder(String id, String userId) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        if (!folder.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied to folder");
        }

        // Check if folder has children or files
        long childCount = folderRepository.countByParentId(id);
        long fileCount = fileRepository.countByFolderId(id);

        if (childCount > 0 || fileCount > 0) {
            throw new RuntimeException("Cannot delete folder with contents. Please delete or move all contents first.");
        }

        folderRepository.delete(folder);
        log.info("Deleted folder: {} by user: {}", id, userId);
    }

    /**
     * Get folder tree structure
     */
    @Transactional(readOnly = true)
    public List<FolderTreeNode> getFolderTree(String userId) {
        List<Folder> rootFolders = folderRepository.findByOwnerIdAndParentIdIsNull(userId);

        return rootFolders.stream()
                .map(folder -> buildFolderTree(folder, userId))
                .collect(Collectors.toList());
    }

    /**
     * Build folder tree recursively
     */
    private FolderTreeNode buildFolderTree(Folder folder, String userId) {
        List<Folder> children = folderRepository.findByParentId(folder.getId());

        List<FolderTreeNode> childNodes = children.stream()
                .filter(f -> f.getOwnerId().equals(userId))
                .map(f -> buildFolderTree(f, userId))
                .collect(Collectors.toList());

        long fileCount = fileRepository.countByFolderId(folder.getId());

        return FolderTreeNode.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParentId())
                .fileCount(fileCount)
                .childCount((long) children.size())
                .children(childNodes)
                .createdAt(folder.getCreatedAt().toString())
                .updatedAt(folder.getUpdatedAt().toString())
                .build();
    }

    /**
     * Check if a folder is descendant of another folder
     */
    private boolean isDescendantOf(Folder folder, String ancestorId) {
        if (folder.getId().equals(ancestorId)) {
            return true;
        }

        if (folder.getParentId() == null) {
            return false;
        }

        Folder parent = folderRepository.findById(folder.getParentId()).orElse(null);
        if (parent == null) {
            return false;
        }

        return isDescendantOf(parent, ancestorId);
    }

    /**
     * Transform Folder to FolderResponse
     */
    private FolderResponse toFolderResponse(Folder folder) {
        long fileCount = fileRepository.countByFolderId(folder.getId());
        long childCount = folderRepository.countByParentId(folder.getId());

        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParentId())
                .fileCount(fileCount)
                .childCount(childCount)
                .createdAt(folder.getCreatedAt().toString())
                .updatedAt(folder.getUpdatedAt().toString())
                .build();
    }

    /**
     * Folder tree node
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FolderTreeNode {
        private String id;
        private String name;
        private String parentId;
        private Long fileCount;
        private Long childCount;
        private List<FolderTreeNode> children;
        private String createdAt;
        private String updatedAt;
    }
}
