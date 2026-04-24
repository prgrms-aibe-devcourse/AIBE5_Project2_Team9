package com.pickkasso.pickkasso.user.dto;

import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class UserReservationDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final Long id;
    private final Long itemId;
    private final String itemName;
    private final String photographerName;
    private final String scheduledAt;
    private final String address;
    private final ReservationStatus status;
    private final String statusLabel;
    private final boolean hasReview;

    public UserReservationDto(Reservation r, boolean hasReview) {
        this.id = r.getId();
        this.itemId = r.getItem().getId();
        this.itemName = r.getItem().getName();
        this.photographerName = r.getItem().getPhotographer().getName();
        this.scheduledAt = r.getScheduledAt().format(FORMATTER);
        this.address = r.getAddress();
        this.status = r.getStatus();
        this.statusLabel = r.getStatus().label();
        this.hasReview = hasReview;
    }
}
