package com.pickkasso.pickkasso.user.entity;


import com.pickkasso.pickkasso.global.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_portfolio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    private PortfolioProjectType projectType;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;


    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioTag> portfolioTagList = new ArrayList<>();
    // 지금의 이미지 관리는, 이미지 삭제를 서비스에서 호출한 다음 update시켜야 함
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImg> portfolioImgList = new ArrayList<>();


    private Portfolio(
        Photographer photographer,
        String name,
        String description,
        PortfolioProjectType projectType,
        LocalDate startDate,
        LocalDate endDate) {
        this.photographer = photographer;
        this.name = name;
        this.description = description;
        this.projectType = projectType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    //== 생성 method ==//
    public static Portfolio createPortfolio(
        Photographer photographer,
        String name,
        String description,
        PortfolioProjectType projectType,
        LocalDate startDate,
        LocalDate endDate) {
        return new Portfolio(photographer, name, description, projectType, startDate, endDate);
    }

    public void updatePortfolio(String name, String description, PortfolioProjectType projectType) {
        this.name = name;
        this.description = description;
        this.projectType = projectType;
    }

    public void updateTags(List<Tag> tagList) {
        this.portfolioTagList.clear();
        tagList.forEach(tag ->
            this.portfolioTagList.add(PortfolioTag.createPortfolioTag(this, tag))
        );
    }

    // 최대 list 길이 20 이하로 가정
    public void updateImgs(List<PortfolioImg> imgList) {
        this.portfolioImgList.clear();
        this.portfolioImgList.addAll(imgList);
    }
}
