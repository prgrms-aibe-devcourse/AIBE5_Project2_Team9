package com.pickkasso.pickkasso.item.entity;

import com.pickkasso.pickkasso.global.img.DefaultImg;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_item_img")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImg extends DefaultImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_img_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;

    private ItemImg(
        Item item,
        String imgName,
        Integer imgOrder
    ) {

        this.item = item;
        this.imgName = imgName;
        this.imgOrder = imgOrder;
    }

    //== 생성 method ==//
    public static ItemImg createItemImg(
        Item item,
        String imgName,
        Integer imgOrder
    ) {
        return new ItemImg(item, imgName, imgOrder);
    }

    public void updateItemImg(String imgName, Integer imgOrder) {
        this.imgName = imgName;
        this.imgOrder = imgOrder;
    }
}