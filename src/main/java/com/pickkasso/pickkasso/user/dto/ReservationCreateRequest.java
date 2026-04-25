package com.pickkasso.pickkasso.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateRequest {
    private Long planId;
    private String scheduledDate;   // YYYY-MM-DD
    private Integer scheduledHour;  // 9 ~ 20
    private String address;
    private String detailAddress;
    private Integer headCount;
    private String memo;
}
