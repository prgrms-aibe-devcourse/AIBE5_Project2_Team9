package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.photographer.CareerDto;
import com.pickkasso.pickkasso.user.dto.photographer.EducationDto;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Career;
import com.pickkasso.pickkasso.user.entity.Education;
import com.pickkasso.pickkasso.user.entity.Gender;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.CareerRepository;
import com.pickkasso.pickkasso.user.repository.EducationRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PhotographerProfileServiceTest {

    @Autowired
    private PhotographerProfileService photographerProfileService;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Test
    @DisplayName("프로필이 없으면 새로 생성된다")
    void createProfile() {
        // given
        Photographer photographer = createTestPhotographer();

        PhotographerProfileEditRequest request = new PhotographerProfileEditRequest(
                "test.jpg",
                "김민준 작가",
                "안녕하세요. 감성 스냅 작가입니다.",
                "https://example.com",
                List.of("Sony A7IV", "85mm f1.4"),
                List.of(
                        new CareerDto(null, "프리랜서 사진작가", LocalDate.of(2020, 1, 1), null)
                ),
                List.of(
                        new EducationDto(null, "서울예술대학교", LocalDate.of(2016, 3, 1), LocalDate.of(2020, 2, 28))
                )
        );

        // when
        photographerProfileService.createOrUpdateProfile(photographer.getId(), request, null);

        // then
        Photographer savedPhotographer = photographerRepository.findById(photographer.getId()).orElseThrow();

        assertThat(savedPhotographer.getPhotographerProfile()).isNotNull();
        assertThat(savedPhotographer.getPhotographerProfile().getNickname()).isEqualTo("김민준 작가");
        assertThat(savedPhotographer.getPhotographerProfile().getIntro()).isEqualTo("안녕하세요. 감성 스냅 작가입니다.");

        List<Career> careers = careerRepository.findByPhotographerId(photographer.getId());
        List<Education> educations = educationRepository.findByPhotographerId(photographer.getId());

        assertThat(careers).hasSize(1);
        assertThat(educations).hasSize(1);
    }

    @Test
    @DisplayName("프로필이 이미 있으면 수정된다")
    void updateProfile() {
        // given
        Photographer photographer = createTestPhotographer();

        PhotographerProfileEditRequest firstRequest = new PhotographerProfileEditRequest(
                "first.jpg",
                "기존 작가명",
                "기존 소개",
                "https://first.com",
                List.of("기존 장비"),
                List.of(),
                List.of()
        );

        photographerProfileService.createOrUpdateProfile(photographer.getId(), firstRequest, null);

        PhotographerProfileEditRequest updateRequest = new PhotographerProfileEditRequest(
                "updated.jpg",
                "수정된 작가명",
                "수정된 소개",
                "https://updated.com",
                List.of("Sony A7IV", "35mm"),
                List.of(
                        new CareerDto(null, "수정 경력", LocalDate.of(2021, 1, 1), null)
                ),
                List.of()
        );

        // when
        photographerProfileService.createOrUpdateProfile(photographer.getId(), updateRequest, null);

        // then
        Photographer savedPhotographer = photographerRepository.findById(photographer.getId()).orElseThrow();

        assertThat(savedPhotographer.getPhotographerProfile().getNickname()).isEqualTo("수정된 작가명");
        assertThat(savedPhotographer.getPhotographerProfile().getIntro()).isEqualTo("수정된 소개");

        List<Career> careers = careerRepository.findByPhotographerId(photographer.getId());
        assertThat(careers).hasSize(1);
        assertThat(careers.get(0).getName()).isEqualTo("수정 경력");
    }

    @Test
    @DisplayName("프로필 조회 시 프로필이 없으면 빈 응답을 준다")
    void getEmptyProfileForm() {
        // given
        Photographer photographer = createTestPhotographer();

        // when
        PhotographerProfileResponse response = photographerProfileService.getProfileForm(photographer.getId());

        // then
        assertThat(response.photographerId()).isEqualTo(photographer.getId());
        assertThat(response.profileId()).isNull();
        assertThat(response.nickname()).isNull();
    }

    private Photographer createTestPhotographer() {
        Account account = Account.createAccount(
                "testuser",
                "encoded-password",
                Role.PHOTOGRAPHER
        );
        accountRepository.save(account);

        Photographer photographer = Photographer.createMember(
                account,
                "test@example.com",
                "홍길동",
                Gender.MALE,
                "010-1234-5678",
                0
        );

        return photographerRepository.save(photographer);
    }
}