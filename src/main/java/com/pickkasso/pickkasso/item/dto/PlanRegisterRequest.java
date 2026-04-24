package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.item.entity.PlanType;
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
    private PlanType planType;
    private Boolean enabled;
    private Integer shootingDuration;
    private Integer originalPhotoCount;
    private Integer editedPhotoCount;
    private Integer deliveryDays;
    private Integer price;
}
