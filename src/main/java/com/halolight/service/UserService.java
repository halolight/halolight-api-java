package com.halolight.service;

import com.halolight.domain.entity.User;
import com.halolight.domain.entity.enums.UserStatus;
import com.halolight.domain.repository.UserRepository;
import com.halolight.dto.UserDTO;
import com.halolight.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * User service for managing user accounts.
 * Uses the new domain structure with String IDs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO getCurrentAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResourceNotFoundException("User", "current", "No authenticated user");
        }
        String userId;
        Object principal = auth.getPrincipal();
        if (principal instanceof com.halolight.security.UserPrincipal up) {
            userId = up.getId();
        } else {
            userId = auth.getName();
        }
        return getUserById(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(UserStatus status, String search, Pageable pageable) {
        return userRepository.findByStatusAndSearch(status, search, pageable)
                .map(this::mapUserToDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapUserToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapUserToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapUserToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update email if provided and changed
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(userDTO.getEmail());
        }

        // Update phone if provided and changed
        if (userDTO.getPhone() != null && !userDTO.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(userDTO.getPhone())) {
                throw new IllegalArgumentException("Phone number is already in use");
            }
            user.setPhone(userDTO.getPhone());
        }

        // Update other fields
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }

        if (userDTO.getAvatar() != null) {
            user.setAvatar(userDTO.getAvatar());
        }

        if (userDTO.getDepartment() != null) {
            user.setDepartment(userDTO.getDepartment());
        }

        if (userDTO.getPosition() != null) {
            user.setPosition(userDTO.getPosition());
        }

        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", user.getUsername());

        return mapUserToDTO(user);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Set user status to inactive instead of hard delete
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }

    @Transactional
    public UserDTO updateStatus(String id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setStatus(status);
        user = userRepository.save(user);
        return mapUserToDTO(user);
    }

    @Transactional
    public int batchDeactivate(Iterable<String> ids) {
        int count = 0;
        for (String id : ids) {
            User user = userRepository.findById(id).orElse(null);
            if (user != null && user.getStatus() != UserStatus.INACTIVE) {
                user.setStatus(UserStatus.INACTIVE);
                userRepository.save(user);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void changePassword(String id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public boolean existsAny() {
        return userRepository.count() > 0;
    }

    /**
     * Maps User entity to UserDTO.
     */
    private UserDTO mapUserToDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .name(user.getName())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .department(user.getDepartment())
                .position(user.getPosition())
                .bio(user.getBio())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
