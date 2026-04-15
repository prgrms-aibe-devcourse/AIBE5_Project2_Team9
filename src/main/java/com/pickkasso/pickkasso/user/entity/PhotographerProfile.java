package com.pickkasso.pickkasso.user.entity;

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
public class PhotographerProfile {
    @Id
    @Column(name = "id_profile")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id_profile", referencedColumnName = "id_photographer")
    private Photographer photographer;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "nickname", nullable = false)
    private String nickname;

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
        String link,
        Boolean verified) {
        this.photographer = photographer;
        this.imgUrl = imgUrl;
        this.nickname = nickname;
        this.intro = intro;
        this.tools = tools;
        this.link = link;
        this.verified = verified;
    }

    //== 생성 method ==//
    public static PhotographerProfile createPhotographerProfile(
        Photographer photographer,
        String imgUrl,
        String nickname,
        String intro,
        Map<String, Object> tools,
        String link,
        Boolean verified) {
        return new PhotographerProfile(photographer, imgUrl, nickname, intro, tools, link, verified);
    }
}
