package com.pickkasso.pickkasso.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BasicInfo {
    @Id
    @Column(name = "id_member")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id_member", referencedColumnName = "id_account")
    private Account account;
    // ...
}
