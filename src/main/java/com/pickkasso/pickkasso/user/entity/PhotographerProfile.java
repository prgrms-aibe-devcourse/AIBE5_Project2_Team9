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
    @Column(name = "intro", nullable = false)
    private String intro;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools", columnDefinition = "json", nullable = false)
    private Map<String, Object> tools;

    @Column(name = "link")
    private String link;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    private PhotographerProfile(
        Photographer photographer,
        String imgUrl,
        String nickname,
        String intro,
        Map<String, Object> tools,
        String link) {
        this.photographer = photographer;
        this.imgUrl = imgUrl;
        this.nickname = nickname;
        this.intro = intro;
        this.tools = tools;
        this.link = link;
        this.verified = false;
        this.imgOrder = 0;
    }

    //== 생성 method ==//
    public static PhotographerProfile createPhotographerProfile(
        Photographer photographer,
        String imgUrl,
        String nickname,
        String intro,
        Map<String, Object> tools,
        String link) {
        return new PhotographerProfile(photographer, imgUrl, nickname, intro, tools, link);
    }

    //기존 프로필 수정용 method
    public void updatePhotographerProfile(
            String imgUrl,
            String nickname,
            String intro,
            Map<String, Object> tools,
            String link,
            Boolean verified

    ) {
        this.imgUrl = imgUrl;
        this.nickname = nickname;
        this.intro = intro;
        this.tools = tools;
        this.link = link;
        this.verified = verified;
    }

    public void verify() {
        this.verified = true;
    }
}
