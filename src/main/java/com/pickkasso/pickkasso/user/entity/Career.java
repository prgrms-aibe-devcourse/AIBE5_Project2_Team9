package com.pickkasso.pickkasso.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "t_career")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Career {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "career_id")
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

    private Career(Photographer photographer, String name, LocalDate startDate) {
        this.photographer = photographer;
        this.name = name;
        this.startDate = startDate;
    }

    private Career(Photographer photographer, String name, LocalDate startDate, LocalDate endDate) {
        this.photographer = photographer;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    //== 생성 method ==//
    public static Career createCareer(Photographer photographer, String name, LocalDate startDate) {
        return new Career(photographer, name, startDate);
    }
    public static Career createCareer(Photographer photographer, String name, LocalDate startDate, LocalDate endDate) {
        return new Career(photographer, name, startDate, endDate);
    }
}
