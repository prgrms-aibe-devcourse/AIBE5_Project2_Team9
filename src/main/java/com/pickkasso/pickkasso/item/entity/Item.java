package com.pickkasso.pickkasso.item.entity;

import com.pickkasso.pickkasso.global.region.Region;
import com.pickkasso.pickkasso.user.entity.Photographer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    // review와 연계해서 자동으로 계산해야 합니다.
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    // review와 연계해서 자동으로 계산해야 합니다.
    // * 100을 저장
    @Column(name = "avg_score", nullable = false)
    private Integer avgScore;

    // plan의 값에 의해 결정됨
    @Column(name = "default_price", nullable = false)
    private Integer defaultPrice;

    @Column(name = "min_booking_lead_time", nullable = false)
    private Integer minBookingLeadTime;

    @Lob
    @Column(name = "cancellation_policy")
    private String cancellationPolicy;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> planList = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemNotice> itemNoticeList = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImg> itemImgList = new ArrayList<>();

    private Item(
        Photographer photographer,
        String name, String description,
        ItemType itemType,
        Integer minBookingLeadTime,
        String cancellationPolicy) {

        this.photographer = photographer;
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.minBookingLeadTime = minBookingLeadTime;
        this.cancellationPolicy = cancellationPolicy;
        reviewCount = 0;
        avgScore = 0;
        defaultPrice = 0;
    }

    //== 생성 method ==//
    public static Item createItem(
        Photographer photographer,
        String name, String description,
        ItemType itemType,
        Integer minBookingLeadTime,
        String cancellationPolicy) {
        return new Item(photographer, name, description, itemType, minBookingLeadTime, cancellationPolicy);
    }

    // plan
    public void addPlan(Plan plan) {
        planList.add(plan);
    }

    public void removePlan(Plan plan) {
        planList.remove(plan);
    }

    public void updateDefaultPrice() {
        defaultPrice = planList.stream()
            .mapToInt(Plan::getPrice)
            .min()
            .orElse(0);
    }

    // item img
    public void addItemImg(ItemImg itemImg) { itemImgList.add(itemImg); }

    public void removeItemImg(ItemImg itemImg) { itemImgList.remove(itemImg); }

    // TODO: img 변경하지 않더라도 삭제하는 로직이다.
    //       나중에 변경되어야 한다.
    public void updateItemImgList(List<ItemImg> newItemImgList) {
        itemImgList.clear();
        itemImgList.addAll(newItemImgList);
    }

    // item notice
    public void addItemNotice(ItemNotice itemNotice) {
        itemNoticeList.add(itemNotice);
    }

    public void removeItemNotice(ItemNotice itemNotice) {
        itemNoticeList.remove(itemNotice);
    }

    // TODO: 위랑 비슷합니다. 변경하지 않더라도 삭제하는 로직이다.
    //       나중에 변경되어야 한다.
    public void updateItemNoticeList(List<ItemNotice> newItemNoticeList) {
        itemNoticeList.clear();
        itemNoticeList.addAll(newItemNoticeList);
    }
}
