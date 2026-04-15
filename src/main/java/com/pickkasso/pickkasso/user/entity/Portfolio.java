package com.pickkasso.pickkasso.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_portfolio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "portfolio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;


    private Portfolio(
        Photographer photographer,
        String name,
        String description) {
        this.photographer = photographer;
        this.name = name;
        this.description = description;
    }

    //== 생성 method ==//
    public static Portfolio createPortfolio(
        Photographer photographer,
        String name,
        String description) {
        return new Portfolio(photographer, name, description);
    }
}
