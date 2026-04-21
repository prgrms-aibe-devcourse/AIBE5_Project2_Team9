package com.pickkasso.pickkasso.global.region;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegionDto {
    private String address;
    private String detailAddress;
    private Double lat;
    private Double lng;

    public static RegionDto from(Region region) {
        return RegionDto.builder()
            .address(region.getAddress())
            .detailAddress(region.getDetailAddress())
            .lat(region.getLat())
            .lng(region.getLng())
            .build();
    }
}
