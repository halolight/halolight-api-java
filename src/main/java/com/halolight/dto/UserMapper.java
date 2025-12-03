package com.halolight.dto;

import com.halolight.domain.entity.User;
import com.halolight.domain.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity to UserDTO conversion.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDTO toDTO(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "quotaUsed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "ownedTeams", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "sharedDocuments", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "folders", ignore = true)
    @Mapping(target = "organizedEvents", ignore = true)
    @Mapping(target = "eventAttendances", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    User toEntity(UserDTO userDTO);

    default Set<String> mapRoles(Set<UserRole> roles) {
        return roles == null ? Set.of() :
                roles.stream()
                        .map(userRole -> userRole.getRole().getName())
                        .collect(Collectors.toSet());
    }
}
