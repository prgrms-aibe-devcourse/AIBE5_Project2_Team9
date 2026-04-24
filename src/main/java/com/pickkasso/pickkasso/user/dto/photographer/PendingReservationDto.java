package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PendingReservationDto(
        Long reservationId,
        String memberName,
        String memberInitial,
        String itemName,
        String scheduledLabel,
        LocalDateTime scheduledAt
) {
    private static final String[] WEEKDAY_KOR = {"월", "화", "수", "목", "금", "토", "일"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static PendingReservationDto of(Reservation r) {
        String memberName = r.getMember().getName();
        String initial = memberName.isEmpty() ? "?" : String.valueOf(memberName.charAt(0));
        LocalDateTime at = r.getScheduledAt();
        String dayOfWeek = WEEKDAY_KOR[at.getDayOfWeek().getValue() - 1];
        String scheduledLabel = at.getMonthValue() + "/" + at.getDayOfMonth()
                + "(" + dayOfWeek + ") " + at.format(TIME_FMT);
        return new PendingReservationDto(
                r.getId(),
                memberName,
                initial,
                r.getItem().getName(),
                scheduledLabel,
                at
        );
    }
}
