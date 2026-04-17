package com.pickkasso.pickkasso.global.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegionDto {
    private Long id;
    private String address;
    private String detailAddress;
    private Double lat;
    private Double lng;
}
