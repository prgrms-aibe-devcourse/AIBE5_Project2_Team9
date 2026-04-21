package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.ItemImg;
import com.pickkasso.pickkasso.item.entity.ItemNotice;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.user.entity.PhotographerProfile;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class ItemDetailDto {
    private Long itemId;
    private String itemName;
    private String description;
    private String itemTypeName;
    private String tagName;
    private String address;
    private String detailAddress;
    private Integer defaultPrice;
    private Integer minBookingLeadTime;
    private String cancellationPolicy;
    private Double avgScore;
    private Integer reviewCount;
    private PhotographerSummary photographer;
    private List<PlanSummary> plans;
    private List<ItemImageSummary> images;
    private List<ItemNoticeSummary> notices;

    public static ItemDetailDto from(Item item) {
        PhotographerProfile profile = item.getPhotographer().getPhotographerProfile();
        String photographerNickname = (profile == null) ? item.getPhotographer().getName() : profile.getNickname();
        String photographerImgUrl = (profile == null) ? null : profile.getImgUrl();
        String photographerIntro = (profile == null) ? null : profile.getIntro();

        List<PlanSummary> plans = item.getPlanList().stream()
            .sorted(Comparator.comparing(Plan::getPrice))
            .map(plan -> PlanSummary.builder()
                .planId(plan.getId())
                .price(plan.getPrice())
                .shootingDuration(plan.getShootingDuration())
                .originalPhotoCount(plan.getOriginalPhotoCount())
                .editedPhotoCount(plan.getEditedPhotoCount())
                .deliveryDays(plan.getDeliveryDays())
                .build())
            .toList();

        List<ItemImageSummary> images = item.getItemImgList().stream()
            .sorted(Comparator.comparing(ItemImg::getImgOrder))
            .map(image -> ItemImageSummary.builder()
                .itemImgId(image.getId())
                .imgUrl(image.getImrUrl())
                .imgOrder(image.getImgOrder())
                .build())
            .toList();

        List<ItemNoticeSummary> notices = item.getItemNoticeList().stream()
            .sorted(Comparator.comparing(ItemNotice::getNoticeOrder))
            .map(notice -> ItemNoticeSummary.builder()
                .itemNoticeId(notice.getId())
                .noticeOrder(notice.getNoticeOrder())
                .description(notice.getDescription())
                .build())
            .toList();

        return ItemDetailDto.builder()
            .itemId(item.getId())
            .itemName(item.getName())
            .description(item.getDescription())
            .itemTypeName(item.getItemType().name())
            .tagName(item.getTag().getName())
            .address(item.getAddress())
            .detailAddress(item.getDetailAddress())
            .defaultPrice(item.getDefaultPrice())
            .minBookingLeadTime(item.getMinBookingLeadTime())
            .cancellationPolicy(item.getCancellationPolicy())
            .avgScore(item.getAvgScore() / 100.0)
            .reviewCount(item.getReviewCount())
            .photographer(PhotographerSummary.builder()
                .photographerId(item.getPhotographer().getId())
                .name(item.getPhotographer().getName())
                .nickname(photographerNickname)
                .profileImgUrl(photographerImgUrl)
                .intro(photographerIntro)
                .build())
            .plans(plans)
            .images(images)
            .notices(notices)
            .build();
    }

    @Getter
    @Builder
    public static class PhotographerSummary {
        private Long photographerId;
        private String name;
        private String nickname;
        private String profileImgUrl;
        private String intro;
    }

    @Getter
    @Builder
    public static class PlanSummary {
        private Long planId;
        private Integer price;
        private Integer shootingDuration;
        private Integer originalPhotoCount;
        private Integer editedPhotoCount;
        private Integer deliveryDays;
    }

    @Getter
    @Builder
    public static class ItemImageSummary {
        private Long itemImgId;
        private String imgUrl;
        private Integer imgOrder;
    }

    @Getter
    @Builder
    public static class ItemNoticeSummary {
        private Long itemNoticeId;
        private Integer noticeOrder;
        private String description;
    }
}
