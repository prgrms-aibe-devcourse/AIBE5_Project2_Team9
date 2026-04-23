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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotographerReservationService {

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
}
