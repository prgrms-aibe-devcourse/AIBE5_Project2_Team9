package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.photographer.CareerDto;
import com.pickkasso.pickkasso.user.dto.photographer.EducationDto;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerPortfolioSummaryDto;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.entity.Career;
import com.pickkasso.pickkasso.user.entity.Education;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.PhotographerProfile;
import com.pickkasso.pickkasso.user.repository.CareerRepository;
import com.pickkasso.pickkasso.user.repository.EducationRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerProfileRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotographerProfileService {

    private final PhotographerRepository photographerRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final CareerRepository careerRepository;
    private final EducationRepository educationRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public PhotographerProfileResponse getProfileForm(Long photographerId) {
        Photographer photographer = getPhotographer(photographerId);

        List<CareerDto> careerDtos = careerRepository.findByPhotographerId(photographerId)
                .stream()
                .map(career -> new CareerDto(
                        career.getId(),
                        career.getName(),
                        career.getStartDate(),
                        career.getEndDate()
                ))
                .toList();

        List<EducationDto> educationDtos = educationRepository.findByPhotographerId(photographerId)
                .stream()
                .map(education -> new EducationDto(
                        education.getId(),
                        education.getName(),
                        education.getStartDate(),
                        education.getEndDate()
                ))
                .toList();

        List<PhotographerPortfolioSummaryDto> portfolios = portfolioRepository.findByPhotographerIdOrderByIdDesc(photographerId)
                .stream()
                .map(portfolio -> new PhotographerPortfolioSummaryDto(
                        portfolio.getId(),
                        portfolio.getName(),
                        portfolio.getDescription()
                ))
                .toList();

        PhotographerProfile profile = photographer.getPhotographerProfile();

        // 프로필이 없으면 빈 폼 데이터 반환
        if (profile == null) {
            return new PhotographerProfileResponse(
                    photographerId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ArrayList<>(),
                    careerDtos,
                    educationDtos,
                    portfolios
            );
        }

        return new PhotographerProfileResponse(
                photographerId,
                profile.getId(),
                profile.getImgUrl(),
                profile.getNickname(),
                profile.getIntro(),
                profile.getLink(),
                convertToolsToList(profile.getTools()),
                careerDtos,
                educationDtos,
                portfolios
        );
    }

    public void createOrUpdateProfile(Long photographerId, PhotographerProfileEditRequest request) {
        Photographer photographer = getPhotographer(photographerId);

        validateRequest(request);

        PhotographerProfile profile = photographer.getPhotographerProfile();

        // profile 이 없으면 새로 생성
        if (profile == null) {
            PhotographerProfile newProfile = PhotographerProfile.createPhotographerProfile(
                    photographer,
                    request.imgUrl(),
                    request.nickname(),
                    request.intro(),
                    convertToolsToMap(request.tools()),
                    request.link(),
                    false
            );

            photographerProfileRepository.save(newProfile);
            photographer.connectProfile(newProfile);
        } else {
            // profile 이 있으면 수정
            profile.updatePhotographerProfile(
                    request.imgUrl(),
                    request.nickname(),
                    request.intro(),
                    convertToolsToMap(request.tools()),
                    request.link(),
                    profile.getVerified()
            );
        }

        // 경력 / 학력은 전체 교체 방식
        replaceCareers(photographer, request.careers());
        replaceEducations(photographer, request.educations());
    }

    private void replaceCareers(Photographer photographer, List<CareerDto> careers) {
        careerRepository.deleteByPhotographerId(photographer.getId());

        if (careers == null || careers.isEmpty()) {
            return;
        }

        List<Career> newCareers = new ArrayList<>();

        for (CareerDto dto : careers) {
            if (dto.name() == null || dto.name().isBlank()) {
                continue;
            }

            Career career;
            if (dto.endDate() == null) {
                career = Career.createCareer(
                        photographer,
                        dto.name(),
                        dto.startDate()
                );
            } else {
                career = Career.createCareer(
                        photographer,
                        dto.name(),
                        dto.startDate(),
                        dto.endDate()
                );
            }

            newCareers.add(career);
        }

        careerRepository.saveAll(newCareers);
    }

    private void replaceEducations(Photographer photographer, List<EducationDto> educations) {
        educationRepository.deleteByPhotographerId(photographer.getId());

        if (educations == null || educations.isEmpty()) {
            return;
        }

        List<Education> newEducations = new ArrayList<>();

        for (EducationDto dto : educations) {
            if (dto.name() == null || dto.name().isBlank()) {
                continue;
            }

            Education education;
            if (dto.endDate() == null) {
                education = Education.createEducation(
                        photographer,
                        dto.name(),
                        dto.startDate()
                );
            } else {
                education = Education.createEducation(
                        photographer,
                        dto.name(),
                        dto.startDate(),
                        dto.endDate()
                );
            }

            newEducations.add(education);
        }

        educationRepository.saveAll(newEducations);
    }

    private Photographer getPhotographer(Long photographerId) {
        return photographerRepository.findById(photographerId)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));
    }

    private void validateRequest(PhotographerProfileEditRequest request) {
        // 최소한의 필수값 검증
        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new IllegalArgumentException("활동명은 필수입니다.");
        }

        if (request.intro() == null || request.intro().isBlank()) {
            throw new IllegalArgumentException("자기소개는 필수입니다.");
        }
    }

    // 장비 리스트를 JSON 저장용 Map 으로 변환
    private Map<String, Object> convertToolsToMap(List<String> tools) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (tools == null || tools.isEmpty()) {
            return result;
        }

        for (int i = 0; i < tools.size(); i++) {
            result.put("tool" + i, tools.get(i));
        }

        return result;
    }

    // 조회한 JSON Map 을 화면용 리스트로 변환
    private List<String> convertToolsToList(Map<String, Object> toolsMap) {
        if (toolsMap == null || toolsMap.isEmpty()) {
            return new ArrayList<>();
        }

        return toolsMap.values().stream()
                .map(String::valueOf)
                .toList();
    }
}