package com.pickkasso.pickkasso.global.img;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class DefaultImg {
    @Column(name = "img_name", nullable = false)
    protected String imgName;

    @Column(name = "img_url", nullable = false, length = 500)
    protected String imgUrl;

    @Column(name = "img_order", nullable = false)
    protected Integer imgOrder;
}