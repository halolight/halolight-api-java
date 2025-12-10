package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.security.UserPrincipal;
import com.halolight.service.TeamService;
import com.halolight.web.dto.team.AddMemberRequest;
import com.halolight.web.dto.team.CreateTeamRequest;
import com.halolight.web.dto.team.TeamResponse;
import com.halolight.web.dto.team.UpdateMemberRoleRequest;
import com.halolight.web.dto.team.UpdateTeamRequest;
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

import java.util.List;

@Tag(name = "Teams", description = "Team management API endpoints")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    @Operation(
            summary = "Get user teams",
            description = "Retrieve all teams that the user owns or is a member of"
    )
    @GetMapping("/my-teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getUserTeams(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        List<TeamResponse> teams = teamService.getUserTeams(user.getId());
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @Operation(
            summary = "Get teams with pagination",
            description = "Retrieve paginated list of teams with optional search"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TeamResponse>>> getTeams(
            @Parameter(description = "Search term for team name or description")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TeamResponse> teams = teamService.getTeams(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @Operation(
            summary = "Get team by ID",
            description = "Retrieve detailed information about a specific team including its members"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(
            @Parameter(description = "Team ID") @PathVariable String id
    ) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    @Operation(
            summary = "Create team",
            description = "Create a new team. The authenticated user will be the team owner."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateTeamRequest request
    ) {
        TeamResponse team = teamService.createTeam(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", team));
    }

    @Operation(
            summary = "Update team",
            description = "Update team information. Only the team owner can update the team."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "Team ID") @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateTeamRequest request
    ) {
        TeamResponse team = teamService.updateTeam(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", team));
    }

    @Operation(
            summary = "Delete team",
            description = "Delete a team. Only the team owner can delete the team."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @Parameter(description = "Team ID") @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        teamService.deleteTeam(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }

    @Operation(
            summary = "Add team member",
            description = "Add a user to the team. Only the team owner can add members."
    )
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<TeamResponse>> addMember(
            @Parameter(description = "Team ID") @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody AddMemberRequest request
    ) {
        TeamResponse team = teamService.addMember(id, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", team));
    }

    @Operation(
            summary = "Remove team member",
            description = "Remove a user from the team. Only the team owner can remove members."
    )
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<TeamResponse>> removeMember(
            @Parameter(description = "Team ID") @PathVariable String id,
            @Parameter(description = "User ID to remove") @PathVariable String userId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        TeamResponse team = teamService.removeMember(id, user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", team));
    }

    @Operation(
            summary = "Update member role",
            description = "Update a team member's role. Only the team owner can update member roles."
    )
    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<ApiResponse<TeamResponse>> updateMemberRole(
            @Parameter(description = "Team ID") @PathVariable String id,
            @Parameter(description = "User ID") @PathVariable String userId,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        TeamResponse team = teamService.updateMemberRole(id, user.getId(), userId, request);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", team));
    }

    @Operation(
            summary = "Get team statistics",
            description = "Retrieve statistics for a specific team"
    )
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<TeamService.TeamStatsResponse>> getTeamStats(
            @Parameter(description = "Team ID") @PathVariable String id
    ) {
        TeamService.TeamStatsResponse stats = teamService.getTeamStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
