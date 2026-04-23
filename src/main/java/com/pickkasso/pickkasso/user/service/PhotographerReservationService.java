package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.photographer.*;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotographerReservationService {
    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_CLOSED = "CLOSED";

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(Long photographerId) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        long todayCount = reservationRepository.countByPhotographerIdAndStatusAndScheduledAtBetween(
                photographerId, ReservationStatus.CONFIRMED, dayStart, dayEnd);
        long pendingCount = reservationRepository.countByPhotographerIdAndStatus(
                photographerId, ReservationStatus.PENDING);

        return new DashboardSummaryDto(todayCount, pendingCount);
    }

    @Transactional(readOnly = true)
    public List<PendingReservationDto> getPendingList(Long photographerId) {
        return reservationRepository
                .findPendingByPhotographer(photographerId, ReservationStatus.PENDING)
                .stream()
                .map(PendingReservationDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TodayScheduleDto> getTodaySchedule(Long photographerId) {
        LocalDate today = LocalDate.now();
        return reservationRepository
                .findInRange(photographerId,
                        today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay(),
                        List.of(ReservationStatus.CONFIRMED))
                .stream()
                .map(TodayScheduleDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocalDate normalizeWeekStart(LocalDate weekStart) {
        LocalDate baseDate = weekStart != null ? weekStart : LocalDate.now();
        int shift = baseDate.getDayOfWeek().getValue() % 7; // 일=0, 월=1, ..., 토=6
        return baseDate.minusDays(shift);
    }

    @Transactional(readOnly = true)
    public WeeklyCalendarDto getWeeklyCalendar(Long photographerId, LocalDate weekStart) {
        LocalDate normalizedWeekStart = normalizeWeekStart(weekStart);
        LocalDate weekEnd = normalizedWeekStart.plusDays(7);

        List<Reservation> reservations = reservationRepository.findInRange(
                photographerId,
                normalizedWeekStart.atStartOfDay(),
                weekEnd.atStartOfDay(),
                List.of(ReservationStatus.CONFIRMED));

        return WeeklyCalendarDto.of(normalizedWeekStart, reservations);
    }

    @Transactional(readOnly = true)
    public ReservationManagementSummaryDto getManagementSummary(Long photographerId) {
        List<Reservation> reservations = reservationRepository.findAllByPhotographerIdForManagement(photographerId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = normalizeWeekStart(today);
        LocalDate weekEnd = weekStart.plusDays(7);

        long totalCount = reservations.size();
        long pendingCount = countByStatus(reservations, ReservationStatus.PENDING);
        long confirmedCountThisWeek = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> !r.getScheduledAt().toLocalDate().isBefore(weekStart) && r.getScheduledAt().toLocalDate().isBefore(weekEnd))
                .count();
        long finalizedCount = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED
                        || r.getStatus() == ReservationStatus.REJECTED
                        || r.getStatus() == ReservationStatus.CANCELED)
                .count();
        long completedCount = countByStatus(reservations, ReservationStatus.COMPLETED);
        int completionRate = finalizedCount == 0 ? 0 : (int) Math.round((completedCount * 100.0) / finalizedCount);
        long urgentPendingCount = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> !r.getRequestedAt().toLocalDate().isAfter(today))
                .count();
        long todayShootCount = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> r.getScheduledAt().toLocalDate().equals(today))
                .count();
        long expectedWeekRevenue = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.COMPLETED)
                .filter(r -> !r.getScheduledAt().toLocalDate().isBefore(weekStart) && r.getScheduledAt().toLocalDate().isBefore(weekEnd))
                .mapToLong(Reservation::getTotalPrice)
                .sum();

        return new ReservationManagementSummaryDto(
                totalCount,
                pendingCount,
                confirmedCountThisWeek,
                completionRate,
                urgentPendingCount,
                todayShootCount,
                expectedWeekRevenue
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationManagementItemDto> getManagementReservations(Long photographerId, String statusFilter, String keyword) {
        String normalizedStatusFilter = normalizeStatusFilter(statusFilter);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.KOREA);

        return reservationRepository.findAllByPhotographerIdForManagement(photographerId).stream()
                .filter(reservation -> matchesStatusFilter(reservation, normalizedStatusFilter))
                .filter(reservation -> matchesKeyword(reservation, normalizedKeyword))
                .sorted(managementComparator())
                .map(ReservationManagementItemDto::of)
                .toList();
    }

    public String normalizeStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return FILTER_ALL;
        }

        return switch (statusFilter.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> ReservationStatus.PENDING.name();
            case "CONFIRMED" -> ReservationStatus.CONFIRMED.name();
            case "COMPLETED" -> ReservationStatus.COMPLETED.name();
            case "REJECTED", "CANCELED", "CLOSED" -> FILTER_CLOSED;
            default -> FILTER_ALL;
        };
    }

    public void approve(Long photographerId, Long reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndPhotographerId(reservationId, photographerId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        reservation.approve();
    }

    public void reject(Long photographerId, Long reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndPhotographerId(reservationId, photographerId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        reservation.reject();
    }

    private long countByStatus(List<Reservation> reservations, ReservationStatus status) {
        return reservations.stream()
                .filter(r -> r.getStatus() == status)
                .count();
    }

    private boolean matchesStatusFilter(Reservation reservation, String statusFilter) {
        if (FILTER_ALL.equals(statusFilter)) {
            return true;
        }
        if (FILTER_CLOSED.equals(statusFilter)) {
            return reservation.getStatus() == ReservationStatus.REJECTED
                    || reservation.getStatus() == ReservationStatus.CANCELED;
        }
        return reservation.getStatus().name().equals(statusFilter);
    }

    private boolean matchesKeyword(Reservation reservation, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        return containsIgnoreCase(reservation.getMember().getName(), normalizedKeyword)
                || containsIgnoreCase(reservation.getItem().getName(), normalizedKeyword)
                || containsIgnoreCase(reservation.getAddress(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String source, String normalizedKeyword) {
        return source != null && source.toLowerCase(Locale.KOREA).contains(normalizedKeyword);
    }

    private Comparator<Reservation> managementComparator() {
        return Comparator
                .comparingInt((Reservation reservation) -> statusPriority(reservation.getStatus()))
                .thenComparing((left, right) -> compareWithinStatus(left, right));
    }

    private int compareWithinStatus(Reservation left, Reservation right) {
        boolean finalized = isFinalized(left.getStatus()) && isFinalized(right.getStatus());
        return finalized
                ? right.getScheduledAt().compareTo(left.getScheduledAt())
                : left.getScheduledAt().compareTo(right.getScheduledAt());
    }

    private boolean isFinalized(ReservationStatus status) {
        return status == ReservationStatus.COMPLETED
                || status == ReservationStatus.REJECTED
                || status == ReservationStatus.CANCELED;
    }

    private int statusPriority(ReservationStatus status) {
        return switch (status) {
            case PENDING -> 0;
            case CONFIRMED -> 1;
            case COMPLETED -> 2;
            case REJECTED, CANCELED -> 3;
        };
    }
}
