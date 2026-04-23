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
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    private PortfolioImg(Portfolio portfolio, String imgName, String imgUrl, Integer imgOrder) {
        this.portfolio = portfolio;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
        this.imgOrder = imgOrder;
    }

    //== 생성 method ==//
    public static PortfolioImg createPortfolioImg(Portfolio portfolio, String imgName, String imgUrl, Integer imgOrder) {
        return new PortfolioImg(portfolio, imgName, imgUrl, imgOrder);
    }

    public void updatePortfolioImg(String imgName, Integer imgOrder) {
        this.imgName = imgName;
        this.imgOrder = imgOrder;
    }

    public void updatePortfolioImg(Integer imgOrder) {
        this.imgOrder = imgOrder;
    }
}
