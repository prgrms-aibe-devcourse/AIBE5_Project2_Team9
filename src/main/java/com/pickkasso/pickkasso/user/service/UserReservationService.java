package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.PlanRepository;
import com.pickkasso.pickkasso.user.dto.ReservationCreateRequest;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;

    @Transactional
    public Long createReservation(Long memberId, ReservationCreateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Plan plan = planRepository.findByIdWithItemAndPhotographer(req.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("패키지를 찾을 수 없습니다."));

        LocalDate date = LocalDate.parse(req.getScheduledDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime scheduledAt = LocalDateTime.of(date, LocalTime.of(req.getScheduledHour(), 0));

        Long photographerId = plan.getItem().getPhotographer().getId();
        LocalDateTime newEnd = scheduledAt.plusHours(plan.getShootingDuration());
        if (reservationRepository.existsOverlapping(photographerId, scheduledAt, newEnd)) {
            throw new IllegalStateException("이미 예약이 차 있는 시간입니다.");
        }

        int price = plan.getPrice();
        member.deductCache(price);

        String fullAddress = req.getAddress() != null ? req.getAddress().trim() : "";
        if (req.getDetailAddress() != null && !req.getDetailAddress().isBlank()) {
            fullAddress = fullAddress + " " + req.getDetailAddress().trim();
        }

        Reservation reservation = Reservation.create(
                member,
                plan.getItem().getPhotographer(),
                plan.getItem(),
                scheduledAt,
                plan.getShootingDuration() * 60,
                fullAddress,
                price,
                req.getMemo()
        );

        return reservationRepository.save(reservation).getId();
    }

    @Transactional(readOnly = true)
    public List<Integer> getBlockedHours(Long photographerId, LocalDate date, int newDurationHours) {
        List<Reservation> existing = reservationRepository.findInRange(
                photographerId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        List<Integer> blocked = new ArrayList<>();
        for (int h = 9; h <= 20; h++) {
            for (Reservation r : existing) {
                int existStart = r.getScheduledAt().getHour();
                int existDuration = r.getDurationMinutes() / 60;
                if (h < existStart + existDuration && existStart < h + newDurationHours) {
                    blocked.add(h);
                    break;
                }
            }
        }
        return blocked;
    }

    @Transactional
    public void cancelByMember(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 예약만 취소할 수 있습니다.");
        }
        reservation.cancel();
        reservation.getMember().refundCache(reservation.getTotalPrice());
    }
}
