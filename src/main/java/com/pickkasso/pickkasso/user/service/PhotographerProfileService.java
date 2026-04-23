package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.global.img.DefaultImgDto;
import com.pickkasso.pickkasso.global.service.DefaultImgService;
import com.pickkasso.pickkasso.global.service.S3Service;
import com.pickkasso.pickkasso.user.dto.photographer.CareerDto;
import com.pickkasso.pickkasso.user.dto.photographer.EducationDto;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerPortfolioSummaryDto;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.entity.*;
import com.pickkasso.pickkasso.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotographerProfileService {

    private final PhotographerRepository photographerRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PortfolioImgRepository portfolioImgRepository;
    private final CareerRepository careerRepository;
    private final EducationRepository educationRepository;
    private final PortfolioRepository portfolioRepository;
    private final DefaultImgService defaultImgService;
    private final S3Service s3Service;

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

        Map<Long, String> imgUrlMap = portfolioImgRepository
            .findRepresentativeImgsByPhotographerId(photographerId)
            .stream()
            .collect(Collectors.toMap(
                img -> img.getPortfolio().getId(),
                PortfolioImg::getImgUrl
            ));

        List<PhotographerPortfolioSummaryDto> portfolios = portfolioRepository.findByPhotographerIdOrderByIdDesc(photographerId)
                .stream()
                .map(portfolio -> new PhotographerPortfolioSummaryDto(
                        portfolio.getId(),
                        portfolio.getName(),
                        portfolio.getDescription(),
                        imgUrlMap.get(portfolio.getId())
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
                    0,
                    0L,
                    0,
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
                profile.getPurchaseCount(),
                profile.getReviewScore(),
                profile.getReviewCount(),
                profile.getContactableStartTime(),
                profile.getContactableEndTime(),
                profile.getResponseTime(),
                convertToolsToList(profile.getTools()),
                careerDtos,
                educationDtos,
                portfolios
        );
    }

    public void createOrUpdateProfile(Long photographerId, PhotographerProfileEditRequest request, MultipartFile profileImg) {
        Photographer photographer = getPhotographer(photographerId);

        validateRequest(request);

        PhotographerProfile profile = photographer.getPhotographerProfile();

        String dirName = "photographer/user_" + photographerId;
        String imgUrl = (profileImg != null && !profileImg.isEmpty())
            ? defaultImgService.uploadImage(profileImg, 0, dirName).getImgUrl()
            : request.imgUrl();
        // profile 이 없으면 새로 생성
        if (profile == null) {
            PhotographerProfile newProfile = PhotographerProfile.createPhotographerProfile(
                    photographer,
                    imgUrl,
                    request.nickname(),
                    request.intro(),
                    convertToolsToMap(request.tools()),
                    request.link(),
                    request.contactableStartTime(),
                    request.contactableEndTime(),
                    request.responseTime()
            );

            photographerProfileRepository.save(newProfile);
            photographer.connectProfile(newProfile);
        } else {
            // profile 이 있으면 수정
            if (profileImg != null && !profileImg.isEmpty() && request.imgUrl() != null) {
                s3Service.delete(request.imgUrl());
            }
            profile.updatePhotographerProfile(
                    imgUrl,
                    request.nickname(),
                    request.intro(),
                    convertToolsToMap(request.tools()),
                    request.link(),
                    profile.getVerified(),
                    request.contactableStartTime(),
                    request.contactableEndTime(),
                    request.responseTime()
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
            if (dto.name() == null || dto.name().isBlank() || dto.startDate() == null) {
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
            if (dto.name() == null || dto.name().isBlank() || dto.startDate() == null) {
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
        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new IllegalArgumentException("활동명은 필수입니다.");
        }

        if (request.intro() == null || request.intro().isBlank()) {
            throw new IllegalArgumentException("자기소개는 필수입니다.");
        }

        validateCareerList(request.careers());
        validateEducationList(request.educations());
        validateToolList(request.tools());
    }

    private void validateCareerList(List<CareerDto> careers) {
        if (careers == null || careers.isEmpty()) {
            throw new IllegalArgumentException("경력을 최소 1개 입력해 주세요.");
        }
        boolean hasValid = false;
        for (CareerDto dto : careers) {
            boolean hasName = dto.name() != null && !dto.name().isBlank();
            boolean hasStart = dto.startDate() != null;
            if (hasName && !hasStart) {
                throw new IllegalArgumentException("경력 항목의 시작 월을 선택해 주세요.");
            }
            if (hasName && hasStart) {
                hasValid = true;
            }
        }
        if (!hasValid) {
            throw new IllegalArgumentException("경력을 최소 1개 입력해 주세요.");
        }
    }

    private void validateEducationList(List<EducationDto> educations) {
        if (educations == null || educations.isEmpty()) {
            throw new IllegalArgumentException("학력을 최소 1개 입력해 주세요.");
        }
        boolean hasValid = false;
        for (EducationDto dto : educations) {
            boolean hasName = dto.name() != null && !dto.name().isBlank();
            boolean hasStart = dto.startDate() != null;
            if (hasName && !hasStart) {
                throw new IllegalArgumentException("학력 항목의 시작 월을 선택해 주세요.");
            }
            if (hasName && hasStart) {
                hasValid = true;
            }
        }
        if (!hasValid) {
            throw new IllegalArgumentException("학력을 최소 1개 입력해 주세요.");
        }
    }

    private void validateToolList(List<String> tools) {
        if (tools == null) {
            throw new IllegalArgumentException("보유 장비를 최소 1개 입력해 주세요.");
        }
        boolean hasValid = tools.stream().anyMatch(t -> t != null && !t.isBlank());
        if (!hasValid) {
            throw new IllegalArgumentException("보유 장비를 최소 1개 입력해 주세요.");
        }
    }

    // 장비 리스트를 JSON 저장용 Map 으로 변환
    private Map<String, Object> convertToolsToMap(List<String> tools) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (tools == null || tools.isEmpty()) {
            return result;
        }

        int idx = 0;
        for (String tool : tools) {
            if (tool == null || tool.isBlank()) continue;
            result.put("tool" + idx, tool);
            idx++;
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