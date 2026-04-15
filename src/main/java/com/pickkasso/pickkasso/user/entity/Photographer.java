package com.pickkasso.pickkasso.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_photographer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photographer extends BasicInfo{
    @Id
    @Column(name = "id_photographer")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id_photographer", referencedColumnName = "id_account")
    private Account account;
    // ...
}
