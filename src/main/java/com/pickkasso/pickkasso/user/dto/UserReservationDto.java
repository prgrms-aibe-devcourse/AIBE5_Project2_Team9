package com.pickkasso.pickkasso.user.dto;

import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class UserReservationDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final Long itemId;
    private final String itemName;
    private final String photographerName;
    private final String scheduledAt;
    private final String address;
    private final ReservationStatus status;
    private final String statusLabel;

    public UserReservationDto(Reservation r) {
        this.id = r.getId();
        this.itemId = r.getItem().getId();
        this.itemName = r.getItem().getName();
        this.photographerName = r.getItem().getPhotographer().getName();
        this.scheduledAt = r.getScheduledAt().format(FORMATTER);
        this.address = r.getAddress();
        this.status = r.getStatus();
        this.statusLabel = toLabel(r.getStatus());
    }

    private static String toLabel(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "예약 대기";
            case CONFIRMED -> "예약 확정";
            case COMPLETED -> "촬영 완료";
            case REJECTED -> "예약 거절";
            case CANCELED -> "예약 취소";
        };
    }
}
