package com.pickkasso.pickkasso.global.scheduler;

import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationAutoCompleteScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void autoComplete() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> candidates = reservationRepository
                .findByStatusAndScheduledAtBefore(ReservationStatus.CONFIRMED, now);

        for (Reservation r : candidates) {
            if (r.getScheduledAt().plusMinutes(r.getDurationMinutes()).isBefore(now)) {
                r.markCompleted();
            }
        }
    }
}
