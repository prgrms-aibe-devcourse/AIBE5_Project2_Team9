package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Portfolio;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PhotographerRepository photographerRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public PortfolioDto getEmptyPortfolioDto(Long photographerId) {
        validatePhotographerExists(photographerId);

        return new PortfolioDto(
                "",
                "",
                null,
                null,
                null,
                null
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
                dto.description()
        );

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return savedPortfolio.getId();
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
