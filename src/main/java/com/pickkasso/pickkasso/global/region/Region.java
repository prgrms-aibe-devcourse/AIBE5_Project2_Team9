package com.pickkasso.pickkasso.global.region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {
    @Id
    @Column(name = "region_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "lat", nullable = false)
    private Double lat;
    @Column(name = "lng", nullable = false)
    private Double lng;


    private Region(String address, String detailAddress, Double lat, Double lng) {
        this.address = address;
        this.detailAddress = detailAddress;
        this.lat = lat;
        this.lng = lng;
    }
}