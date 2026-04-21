package com.pickkasso.pickkasso.global.region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
public class Region {
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    protected void initRegion(String address, String detailAddress, Double lat, Double lng) {
        this.address = (address != null) ? address : "";
        this.detailAddress = (detailAddress != null) ? detailAddress : "";
        this.lat = (lat != null) ? lat : 0.0;
        this.lng = (lng != null) ? lng : 0.0;
    }
}