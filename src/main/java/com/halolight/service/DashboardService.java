package com.halolight.service;

import com.halolight.dto.ChartDataDTO;
import com.halolight.dto.DashboardStatsDTO;
import com.halolight.domain.entity.enums.UserStatus;
import com.halolight.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for dashboard statistics and charts.
 * Updated to use the new domain structure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;

    /**
     * Get dashboard statistics.
     * Cached for 5 minutes to reduce database load.
     */
    @Cacheable(value = "dashboardStats", unless = "#result == null")
    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        log.debug("Calculating dashboard statistics");

        long totalUsers = userRepository.count();

        // Use a custom query or filter in memory for now
        // This is a simplified version - you may want to add custom repository methods
        long activeUsers = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .count();

        long inactiveUsers = totalUsers - activeUsers;

        Instant now = Instant.now();
        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        Instant startOfWeek = now.minus(7, ChronoUnit.DAYS);
        Instant startOfMonth = now.minus(30, ChronoUnit.DAYS);

        // These would ideally be custom repository methods
        long newUsersToday = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(startOfDay))
                .count();

        long newUsersThisWeek = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(startOfWeek))
                .count();

        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(startOfMonth))
                .count();

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .averageLoginPerUser(0.0) // Placeholder for future implementation
                .totalSessions(0L) // Placeholder for future implementation
                .build();
    }

    /**
     * Get user registration chart data for the last 7 days.
     * Cached for 1 hour.
     */
    @Cacheable(value = "userRegistrationChart", unless = "#result == null")
    @Transactional(readOnly = true)
    public ChartDataDTO getUserRegistrationChart() {
        log.debug("Generating user registration chart data");

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        Instant now = Instant.now();

        // Get data for last 7 days
        for (int i = 6; i >= 0; i--) {
            Instant startOfDay = now.minus(i, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

            long count = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isAfter(startOfDay) && user.getCreatedAt().isBefore(endOfDay))
                    .count();

            labels.add(startOfDay.toString().substring(0, 10));
            data.add(count);
        }

        ChartDataDTO.DatasetDTO dataset = ChartDataDTO.DatasetDTO.builder()
                .label("New Users")
                .data(data)
                .backgroundColor("rgba(75, 192, 192, 0.2)")
                .borderColor("rgba(75, 192, 192, 1)")
                .build();

        return ChartDataDTO.builder()
                .labels(labels)
                .datasets(List.of(dataset))
                .build();
    }

    /**
     * Get user activity chart data for the last 7 days.
     * Cached for 1 hour.
     */
    @Cacheable(value = "userActivityChart", unless = "#result == null")
    @Transactional(readOnly = true)
    public ChartDataDTO getUserActivityChart() {
        log.debug("Generating user activity chart data");

        List<String> labels = new ArrayList<>();
        List<Long> activeData = new ArrayList<>();
        List<Long> inactiveData = new ArrayList<>();

        Instant now = Instant.now();

        // Get data for last 7 days
        for (int i = 6; i >= 0; i--) {
            Instant startOfDay = now.minus(i, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

            long activeCount = userRepository.findAll().stream()
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE &&
                            user.getLastLoginAt() != null &&
                            user.getLastLoginAt().isAfter(startOfDay) &&
                            user.getLastLoginAt().isBefore(endOfDay))
                    .count();

            long totalCount = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isBefore(endOfDay))
                    .count();

            long inactiveCount = totalCount - activeCount;

            labels.add(startOfDay.toString().substring(0, 10));
            activeData.add(activeCount);
            inactiveData.add(inactiveCount);
        }

        ChartDataDTO.DatasetDTO activeDataset = ChartDataDTO.DatasetDTO.builder()
                .label("Active Users")
                .data(activeData)
                .backgroundColor("rgba(75, 192, 192, 0.2)")
                .borderColor("rgba(75, 192, 192, 1)")
                .build();

        ChartDataDTO.DatasetDTO inactiveDataset = ChartDataDTO.DatasetDTO.builder()
                .label("Inactive Users")
                .data(inactiveData)
                .backgroundColor("rgba(255, 99, 132, 0.2)")
                .borderColor("rgba(255, 99, 132, 1)")
                .build();

        return ChartDataDTO.builder()
                .labels(labels)
                .datasets(List.of(activeDataset, inactiveDataset))
                .build();
    }
}
