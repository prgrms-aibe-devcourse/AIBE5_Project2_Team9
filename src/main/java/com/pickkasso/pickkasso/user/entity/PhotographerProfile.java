package com.pickkasso.pickkasso.user.entity;

import com.pickkasso.pickkasso.global.img.DefaultImg;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "t_photographer_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotographerProfile extends DefaultImg {
    @Id
    @Column(name = "profile_id")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "profile_id")
    private Photographer photographer;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Lob
    @Column(name = "intro", nullable = false, columnDefinition = "TEXT")
    private String intro;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools", columnDefinition = "json", nullable = false)
    private Map<String, Object> tools;

    @Column(name = "link")
    private String link;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @Column(name = "purchase_count")
    private Integer purchaseCount;

    @Column(name = "review_score")
    private Long reviewScore;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "contactable_start_time")
    private Integer contactableStartTime;

    @Column(name = "contactable_end_time")
    private Integer contactableEndTime;

    @Column(name = "responseTime")
    @Enumerated(EnumType.STRING)
    private ResponseTime responseTime;

    private PhotographerProfile(
        Photographer photographer,
        String imgUrl,
        String nickname,
        String intro,
        Map<String, Object> tools,
        String link,
        Integer contactableStartTime,
        Integer contactableEndTime,
        ResponseTime responseTime) {
        this.photographer = photographer;
        this.imgUrl = imgUrl;
        this.nickname = nickname;
        this.intro = intro;
        this.tools = tools;
        this.link = link;
        this.verified = false;
        this.imgOrder = 0;
        this.purchaseCount = 0;
        this.reviewScore = 0L;
        this.reviewCount = 0;
        this.contactableStartTime = contactableStartTime;
        this.contactableEndTime = contactableEndTime;
        this.responseTime = responseTime;
    }

    //== 생성 method ==//
    public static PhotographerProfile createPhotographerProfile(
        Photographer photographer,
        String imgUrl,
        String nickname,
        String intro,
        Map<String, Object> tools,
        String link,
        Integer contactableStartTime,
        Integer contactableEndTime,
        ResponseTime responseTime) {
        return new PhotographerProfile(photographer, imgUrl, nickname, intro, tools, link, contactableStartTime, contactableEndTime, responseTime);
    }

    //기존 프로필 수정용 method
    public void updatePhotographerProfile(
            String imgUrl,
            String nickname,
            String intro,
            Map<String, Object> tools,
            String link,
            Boolean verified,
            Integer contactableStartTime,
            Integer contactableEndTime,
            ResponseTime responseTime
    ) {
        this.imgUrl = imgUrl;
        this.nickname = nickname;
        this.intro = intro;
        this.tools = tools;
        this.link = link;
        this.verified = verified;
        this.contactableStartTime = contactableStartTime;
        this.contactableEndTime = contactableEndTime;
        this.responseTime = responseTime;
    }

    public void verify() {
        this.verified = true;
    }
}
