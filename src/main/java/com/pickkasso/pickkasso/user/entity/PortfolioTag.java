package com.pickkasso.pickkasso.user.entity;

import com.pickkasso.pickkasso.global.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "t_portfolio_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    private PortfolioTag(Portfolio portfolio, Tag tag) {
        this.portfolio = portfolio;
        this.tag = tag;
    }

    //== 생성 method ==//
    public static PortfolioTag createPortfolioTag(
        Portfolio portfolio,
        Tag tag
    ) {
        return new PortfolioTag(portfolio, tag);
    }
}
