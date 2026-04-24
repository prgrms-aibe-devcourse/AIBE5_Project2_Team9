package com.pickkasso.pickkasso.user.dto.photographer;

public record ReservationManagementSummaryDto(
        long totalCount,
        long pendingCount,
        long confirmedCountThisWeek,
        int completionRate,
        long urgentPendingCount,
        long todayShootCount,
        long expectedWeekRevenue
) {}
