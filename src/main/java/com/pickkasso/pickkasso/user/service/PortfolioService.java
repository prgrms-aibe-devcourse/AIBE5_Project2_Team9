package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.global.tag.TagRepository;
import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Portfolio;
import com.pickkasso.pickkasso.user.entity.PortfolioProjectType;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PhotographerRepository photographerRepository;
    private final PortfolioRepository portfolioRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public PortfolioDto getEmptyPortfolioDto(Long photographerId) {
        validatePhotographerExists(photographerId);

        return new PortfolioDto(
                "",
                "",
                null,
                null,
                null,
                null,
                PortfolioProjectType.PERSONAL
        );
    }

    public Long createPortfolio(Long photographerId, PortfolioDto dto) {
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));

        validatePortfolio(dto);

        // 현재 Portfolio 엔티티 필드에 맞춰 최소 저장
        Portfolio portfolio = Portfolio.createPortfolio(
                photographer,
                dto.name(),
                dto.description(),
                resolveProjectType(dto)
        );
        portfolio.updateTags(getTags(dto.tagIdList()));

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return savedPortfolio.getId();
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPortfolioDto(Long photographerId, Long portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(photographerId, portfolioId);
        return toDto(portfolio);
    }

    @Transactional(readOnly = true)
    public List<String> getPortfolioTagNames(Long photographerId, Long portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(photographerId, portfolioId);
        return portfolio.getTags().stream()
                .map(Tag::getName)
                .toList();
    }

    public void updatePortfolio(Long photographerId, Long portfolioId, PortfolioDto dto) {
        validatePortfolio(dto);

        Portfolio portfolio = getOwnedPortfolio(photographerId, portfolioId);
        portfolio.updatePortfolio(dto.name(), dto.description(), resolveProjectType(dto));
        portfolio.updateTags(getTags(dto.tagIdList()));
    }

    public void deletePortfolio(Long photographerId, Long portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(photographerId, portfolioId);
        portfolio.updateTags(List.of());
        portfolioRepository.delete(portfolio);
    }

    private Portfolio getOwnedPortfolio(Long photographerId, Long portfolioId) {
        return portfolioRepository.findByIdAndPhotographerId(portfolioId, photographerId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));
    }

    private PortfolioDto toDto(Portfolio portfolio) {
        return new PortfolioDto(
                portfolio.getName(),
                portfolio.getDescription(),
                null,
                null,
                null,
                portfolio.getTags().stream().map(Tag::getId).toList(),
                portfolio.getProjectType()
        );
    }

    private PortfolioProjectType resolveProjectType(PortfolioDto dto) {
        if (dto.projectType() == null) {
            return PortfolioProjectType.PERSONAL;
        }
        return dto.projectType();
    }

    private List<Tag> getTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return tagRepository.findAllById(tagIds);
    }

    private void validatePhotographerExists(Long photographerId) {
        if (!photographerRepository.existsById(photographerId)) {
            throw new IllegalArgumentException("작가를 찾을 수 없습니다.");
        }
    }

    private void validatePortfolio(PortfolioDto dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("포트폴리오 이름은 필수입니다.");
        }

        if (dto.description() == null || dto.description().isBlank()) {
            throw new IllegalArgumentException("포트폴리오 설명은 필수입니다.");
        }
    }
}
