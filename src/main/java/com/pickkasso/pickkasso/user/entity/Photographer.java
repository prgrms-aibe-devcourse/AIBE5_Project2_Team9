package com.pickkasso.pickkasso.user.entity;


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
    @Column(name = "id_photographer")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id_photographer", referencedColumnName = "id_account")
    private Account account;

    @OneToOne(mappedBy = "photographer", cascade = CascadeType.ALL)
    private PhotographerProfile photographerProfile;

    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Portfolio> portfolioList = new ArrayList<>();
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Education> educationList = new ArrayList<>();
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL)
    private List<Career> careerList = new ArrayList<>();

    private Photographer(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cache) {
        this.account = account;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.cache = cache;
    }

    //== 생성 method ==//
    public static Photographer createMember(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cache) {
        return new Photographer(account, email, name, gender, phone, cache);
    }
}