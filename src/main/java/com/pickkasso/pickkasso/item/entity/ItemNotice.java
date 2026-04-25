package com.pickkasso.pickkasso.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="t_item_notice")
@Getter
@NoArgsConstructor
public class ItemNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_notice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "notice_order")
    private Integer noticeOrder;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private ItemNotice(
        Item item,
        Integer noticeOrder,
        String description
    ) {
        this.item = item;
        this.noticeOrder = noticeOrder;
        this.description = description;
    }

    //== 생성 method ==//
    public static ItemNotice createItemNotice(
        Item item,
        Integer noticeOrder,
        String description
    ) {
        return new ItemNotice(item, noticeOrder, description);
    }

    public void updateItem(Integer noticeOrder, String description) {
        this.noticeOrder = noticeOrder;
        this.description = description;
    }
}
