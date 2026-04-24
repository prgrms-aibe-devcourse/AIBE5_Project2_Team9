package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.PlanRepository;
import com.pickkasso.pickkasso.user.dto.ReservationCreateRequest;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
                plan.getPrice(),
                req.getMemo()
        );

        return reservationRepository.save(reservation).getId();
    }
}
