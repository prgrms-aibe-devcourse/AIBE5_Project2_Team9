package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
import com.pickkasso.pickkasso.user.entity.PhotographerProfile;
import com.pickkasso.pickkasso.user.entity.Portfolio;
import com.pickkasso.pickkasso.user.repository.PortfolioRepository;
import com.pickkasso.pickkasso.user.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioRepository portfolioRepository;

    @GetMapping("/new")
    public String createPortfolioForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("portfolioDto", portfolioService.getEmptyPortfolioDto(photographerId));
        model.addAttribute("photographerId", photographerId);
        return "photographer/editPortfolio";
    }

    @PostMapping("/new")
    public String createPortfolio(
            @PathVariable Long photographerId,
            @ModelAttribute("portfolioDto") PortfolioDto portfolioDto,
            Model model
    ) {
        try {
            Long portfolioId = portfolioService.createPortfolio(photographerId, portfolioDto);
            return "redirect:/photographer/" + photographerId + "/portfolio/" + portfolioId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("portfolioDto", portfolioDto);
            model.addAttribute("photographerId", photographerId);
            return "photographer/editPortfolio";
        }
    }

    @GetMapping("/{portfolioId}")
    public String getPortfolio(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            Model model
    ) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));

        PhotographerProfile profile = portfolio.getPhotographer().getPhotographerProfile();

        model.addAttribute("portfolioName", portfolio.getName());
        model.addAttribute("portfolioDescription", portfolio.getDescription());
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("photographerNickname", profile != null ? profile.getNickname() : "");
        model.addAttribute("photographerImgUrl", profile != null ? profile.getImgUrl() : null);
        model.addAttribute("photographerRating", 0.0);
        model.addAttribute("photographerReviewCount", 0);
        return "photographer/portfolio-detail";
    }
}