package com.pickkasso.pickkasso.user.entity;


import com.pickkasso.pickkasso.global.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    private PortfolioProjectType projectType;

    @ManyToMany
    @JoinTable(
            name = "t_portfolio_tag",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();


    private Portfolio(
        Photographer photographer,
        String name,
        String description,
        PortfolioProjectType projectType) {
        this.photographer = photographer;
        this.name = name;
        this.description = description;
        this.projectType = projectType;
    }

    //== 생성 method ==//
    public static Portfolio createPortfolio(
        Photographer photographer,
        String name,
        String description,
        PortfolioProjectType projectType) {
        return new Portfolio(photographer, name, description, projectType);
    }

    public void updatePortfolio(String name, String description, PortfolioProjectType projectType) {
        this.name = name;
        this.description = description;
        this.projectType = projectType;
    }

    public void updateTags(List<Tag> tags) {
        this.tags.clear();
        if (tags == null || tags.isEmpty()) {
            return;
        }
        this.tags.addAll(tags);
    }
}
