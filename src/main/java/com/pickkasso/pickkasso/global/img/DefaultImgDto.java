package com.pickkasso.pickkasso.global.img;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DefaultImgDto {
    private String imgUrl;
    private Integer imgOrder;

    public static DefaultImgDto from(DefaultImg defaultImg) {
        return DefaultImgDto.builder()
            .imgUrl(defaultImg.getImgUrl())
            .imgOrder(defaultImg.getImgOrder())
            .build();
    }
}