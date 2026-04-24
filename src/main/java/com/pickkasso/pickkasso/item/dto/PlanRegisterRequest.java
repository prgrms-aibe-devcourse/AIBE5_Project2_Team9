package com.pickkasso.pickkasso.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanRegisterRequest {
    private String planName;
    private Integer shootingDuration;
    private Integer originalPhotoCount;
    private Integer editedPhotoCount;
    private Integer deliveryDays;
    private Integer price;
}
