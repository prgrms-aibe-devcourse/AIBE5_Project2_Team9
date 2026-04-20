package com.pickkasso.pickkasso.global.region;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegionDto {
    private String address;
    private String detailAddress;
    private Double lat;
    private Double lng;

    @QueryProjection
    public RegionDto(String address, String detailAddress, Double lat, Double lng) {
        this.address = address;
        this.detailAddress = detailAddress;
        this.lat = lat;
        this.lng = lng;
    }

    public static RegionDto from(Region region) {
        return RegionDto.builder()
            .address(region.getAddress())
            .detailAddress(region.getDetailAddress())
            .lat(region.getLat())
            .lng(region.getLng())
            .build();
    }
}
