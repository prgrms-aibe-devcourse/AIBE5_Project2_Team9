package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Reservation;

public record TodayScheduleDto(
        Long reservationId,
        String timeLabel,
        String ampm,
        String itemName,
        String address,
        String memberName,
        String dotColorHex
) {
    private static final String[] DOT_COLORS = {"#1D3CFF", "#22C55E", "#FF6B2B", "#0097FF"};

    public static TodayScheduleDto of(Reservation r) {
        int hour = r.getScheduledAt().getHour();
        String timeLabel = String.format("%02d:%02d", hour, r.getScheduledAt().getMinute());
        String ampm = hour < 12 ? "AM" : "PM";
        String dotColor = DOT_COLORS[(int) (r.getItem().getId() % 4)];
        return new TodayScheduleDto(
                r.getId(),
                timeLabel,
                ampm,
                r.getItem().getName(),
                r.getAddress(),
                r.getMember().getName(),
                dotColor
        );
    }
}
