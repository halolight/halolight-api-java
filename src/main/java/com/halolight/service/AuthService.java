package com.halolight.service;

import com.halolight.domain.entity.RefreshToken;
import com.halolight.domain.entity.Role;
import com.halolight.domain.entity.User;
import com.halolight.domain.entity.UserRole;
import com.halolight.domain.entity.enums.UserStatus;
import com.halolight.domain.entity.id.UserRoleId;
import com.halolight.domain.repository.RefreshTokenRepository;
import com.halolight.domain.repository.RoleRepository;
import com.halolight.domain.repository.UserRepository;
import com.halolight.dto.*;
import com.halolight.exception.AuthenticationException;
import com.halolight.exception.ResourceNotFoundException;
import com.halolight.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication service handling user login, registration, token refresh, and logout.
 * Uses the new domain structure with String IDs and domain entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     * Authenticates a user with username/email and password.
     *
     * @param loginRequest containing username/email and password
     * @return AuthResponse with access token, refresh token, and user info
     * @throws AuthenticationException if credentials are invalid or user is inactive
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.debug("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        // Find user by username or email
        User user = userRepository.findByEmail(loginRequest.getUsernameOrEmail())
                .or(() -> userRepository.findByUsername(loginRequest.getUsernameOrEmail()))
                .orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", loginRequest.getUsernameOrEmail());
            throw new AuthenticationException("Invalid username/email or password");
        }

        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt for inactive user: {}", user.getUsername());
            throw new AuthenticationException("Account is not active. Please contact support.");
        }

        // Update last login timestamp
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save refresh token to database
        RefreshToken refreshToken = createRefreshToken(user.getId(), refreshTokenValue);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully: {}", user.getUsername());

        return buildAuthResponse(user, accessToken, refreshTokenValue);
    }

    /**
     * Registers a new user account.
     *
     * @param registerRequest containing user registration details
     * @return AuthResponse with access token, refresh token, and user info
     * @throws AuthenticationException if username or email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.debug("Attempting to register user: {}", registerRequest.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AuthenticationException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthenticationException("Email is already in use");
        }

        // Check if phone already exists (if provided)
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().isBlank()) {
            if (userRepository.existsByPhone(registerRequest.getPhone())) {
                throw new AuthenticationException("Phone number is already in use");
            }
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .phone(registerRequest.getPhone())
                .department(registerRequest.getDepartment())
                .position(registerRequest.getPosition())
                .status(UserStatus.ACTIVE)
                .roles(new LinkedHashSet<>())
                .build();

        // Save user first to generate ID
        user = userRepository.save(user);

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> createDefaultUserRole());

        UserRole userRoleEntity = UserRole.builder()
                .id(new UserRoleId(user.getId(), userRole.getId()))
                .user(user)
                .role(userRole)
                .build();

        user.getRoles().add(userRoleEntity);
        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save refresh token to database
        RefreshToken refreshToken = createRefreshToken(user.getId(), refreshTokenValue);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, accessToken, refreshTokenValue);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        return userService.getCurrentAuthenticatedUser();
    }

    /**
     * Placeholder for sending reset instructions. In production integrate email + token store.
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> log.info("Password reset requested for {}", email));
        // No-op to avoid leaking whether email exists.
    }

    /**
     * Placeholder for resetting password by token. Here we simply reject invalid token pattern and log.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // In a real implementation validate token, find user by token, then update password.
        log.warn("Password reset called with token {} (placeholder implementation)", token);
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
        // Placeholder: simply return to acknowledge request without changing anything.
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshTokenValue the refresh token string
     * @return AuthResponse with new access token and refresh token
     * @throws AuthenticationException if refresh token is invalid, expired, or revoked
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        log.debug("Attempting to refresh token");

        // Validate token format
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        // Get user ID from token
        String userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found or has been revoked"));

        // Check if token is expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired");
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        // Revoke old refresh token
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save new refresh token
        RefreshToken newRefreshToken = createRefreshToken(user.getId(), newRefreshTokenValue);
        refreshTokenRepository.save(newRefreshToken);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return buildAuthResponse(user, newAccessToken, newRefreshTokenValue);
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param refreshTokenValue the refresh token to revoke
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        log.debug("Attempting to logout user");

        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out successfully");
                });
    }

    /**
     * Logs out a user from all devices by revoking all refresh tokens.
     *
     * @param userId the user ID
     */
    @Transactional
    public void logoutFromAllDevices(String userId) {
        log.debug("Attempting to logout user from all devices: {}", userId);

        int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Logged out user from {} devices", revokedCount);
    }

    /**
     * Creates a RefreshToken entity.
     */
    private RefreshToken createRefreshToken(String userId, String tokenValue) {
        return RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration()))
                .isRevoked(false)
                .build();
    }

    /**
     * Builds an AuthResponse from user and tokens.
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserDTO userDTO = mapUserToDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(userDTO)
                .build();
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

    /**
     * Creates a default USER role if it doesn't exist.
     */
    private Role createDefaultUserRole() {
        log.warn("USER role not found, creating default role");

        Role role = Role.builder()
                .name("USER")
                .label("User")
                .description("Default user role")
                .permissions(new LinkedHashSet<>())
                .users(new LinkedHashSet<>())
                .build();

        return roleRepository.save(role);
    }
}
