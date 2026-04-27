package com.pickkasso.pickkasso.user.entity;

import com.pickkasso.pickkasso.global.img.DefaultImg;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "t_portfolio_img")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioImg extends DefaultImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_img_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    private PortfolioImg(Portfolio portfolio, String imgUrl, Integer imgOrder) {
        this.portfolio = portfolio;
        this.imgUrl = imgUrl;
        this.imgOrder = imgOrder;
    }

    //== 생성 method ==//
    public static PortfolioImg createPortfolioImg(Portfolio portfolio, String imgUrl, Integer imgOrder) {
        return new PortfolioImg(portfolio, imgUrl, imgOrder);
    }

    public void updatePortfolioImg(Integer imgOrder) {
        this.imgOrder = imgOrder;
    }
}
