package com.pickkasso.pickkasso.user.entity;


import com.pickkasso.pickkasso.item.entity.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_photographer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photographer extends UserBasicInfo {
    @Id
    @Column(name = "photographer_id")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "photographer_id")
    private Account account;

    @OneToOne(mappedBy = "photographer", cascade = CascadeType.ALL)
    private PhotographerProfile photographerProfile;

    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Portfolio> portfolioList = new ArrayList<>();
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Education> educationList = new ArrayList<>();
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Career> careerList = new ArrayList<>();
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Item> itemList = new ArrayList<>();

    private Photographer(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cash) {
        this.account = account;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.cash = cash;
    }

    //== 생성 method ==//
    public static Photographer createMember(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cash) {
        return new Photographer(account, email, name, gender, phone, cash);
    }

    // 양방향 관계 연결용
    public void connectProfile(PhotographerProfile photographerProfile) {
        this.photographerProfile = photographerProfile;
    }

    public void addItem(Item item) { itemList.add(item); }

    public void removeItem(Item item) { itemList.remove(item); }
}