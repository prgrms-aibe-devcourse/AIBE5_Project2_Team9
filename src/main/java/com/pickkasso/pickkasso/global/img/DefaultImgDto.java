package com.pickkasso.pickkasso.global.img;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultImgDto {
    private String imgName;
    private String imrUrl;
    private Integer imgOrder;
    private LocalDateTime regTime;
}