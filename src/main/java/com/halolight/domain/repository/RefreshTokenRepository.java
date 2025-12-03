package com.halolight.domain.repository;

import com.halolight.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    List<RefreshToken> findByUserId(String userId);

    List<RefreshToken> findByUserIdAndIsRevokedFalse(String userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.userId = :userId")
    int revokeAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.token = :token")
    int revokeByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.isRevoked = true")
    int deleteExpiredOrRevoked(@Param("now") Instant now);

    long countByUserIdAndIsRevokedFalse(String userId);
}
