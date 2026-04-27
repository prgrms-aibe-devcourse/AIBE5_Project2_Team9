package com.pickkasso.pickkasso.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "plan_type")
    private PlanType planType;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "name")
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name="shooting_duration", nullable = false)
    private Integer shootingDuration;

    @Column(name="original_photo_count", nullable = false)
    private Integer originalPhotoCount;

    @Column(name="edited_photo_count", nullable = false)
    private Integer editedPhotoCount;

    @Column(name="delivery_days", nullable = false)
    private Integer deliveryDays;

    private Plan(
        Item item,
        PlanType planType,
        Boolean enabled,
        String name,
        Integer price,
        Integer shootingDuration,
        Integer originalPhotoCount,
        Integer editedPhotoCount,
        Integer deliveryDays) {

        this.planType = planType;
        this.enabled = (enabled != null) ? enabled : false;
        this.item = item;
        this.name = name;
        this.price = price;
        this.shootingDuration = shootingDuration;
        this.originalPhotoCount = originalPhotoCount;
        this.editedPhotoCount = editedPhotoCount;
        this.deliveryDays = deliveryDays;
    }

    //== 생성 method ==//
    public static Plan createPlan(
        Item item,
        PlanType planType,
        Boolean enabled,
        String name,
        Integer price,
        Integer shootingDuration,
        Integer originalPhotoCount,
        Integer editedPhotoCount,
        Integer deliveryDays) {

        Plan plan = new Plan(item, planType, enabled, name, price, shootingDuration, originalPhotoCount, editedPhotoCount, deliveryDays);
        plan.item.addPlan(plan);
        plan.item.updateDefaultPrice();

        return plan;
    }

    public void updatePlan(
        Boolean enabled,
        String name,
        Integer price,
        Integer shootingDuration,
        Integer originalPhotoCount,
        Integer editedPhotoCount,
        Integer deliveryDays) {
        this.enabled = enabled;
        this.name = name;
        setPrice(price);
        this.shootingDuration = shootingDuration;
        this.originalPhotoCount = originalPhotoCount;
        this.editedPhotoCount = editedPhotoCount;
        this.deliveryDays = deliveryDays;
    }


    public void setPrice(Integer price) {
        this.price = price;
        this.item.updateDefaultPrice();
    }

    public void deletePlan() {
        this.item.removePlan(this);
        this.item.updateDefaultPrice();
        this.item = null;
    }
}
