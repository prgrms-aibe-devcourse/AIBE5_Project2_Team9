package com.pickkasso.pickkasso.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "t_education")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "education_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Education(Photographer photographer, String name, LocalDate startDate) {
        this.photographer = photographer;
        this.name = name;
        this.startDate = startDate;
    }

    private Education(Photographer photographer, String name, LocalDate startDate, LocalDate endDate) {
        this.photographer = photographer;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    //== 생성 method ==//
    public Education createCareer(Photographer photographer, String name, LocalDate startDate) {
        return new Education(photographer, name, startDate);
    }
    public Education createCareer(Photographer photographer, String name, LocalDate startDate, LocalDate endDate) {
        return new Education(photographer, name, startDate, endDate);
    }
}
