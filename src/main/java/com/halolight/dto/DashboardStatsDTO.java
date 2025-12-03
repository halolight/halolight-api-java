package com.halolight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Dashboard statistics data transfer object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO implements Serializable {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Double averageLoginPerUser;
    private Long totalSessions;
}
