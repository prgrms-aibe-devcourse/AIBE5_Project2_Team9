package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
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

    @GetMapping("/new")
    public String createPortfolioForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("portfolioDto", portfolioService.getEmptyPortfolioDto(photographerId));
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/new");
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
            model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/new");
            return "photographer/editPortfolio";
        }
    }

    @GetMapping("/{portfolioId}")
    public String portfolioDetail(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            Model model
    ) {
        PortfolioDto portfolioDto = portfolioService.getPortfolioDto(photographerId, portfolioId);
        model.addAttribute("portfolioName", portfolioDto.name());
        model.addAttribute("portfolioDescription", portfolioDto.description());
        model.addAttribute("portfolioTagNames", portfolioService.getPortfolioTagNames(photographerId, portfolioId));
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("photographerNickname", "");
        model.addAttribute("photographerImgUrl", null);
        model.addAttribute("photographerRating", 0.0);
        model.addAttribute("photographerReviewCount", 0);
        return "photographer/portfolio-detail";
    }

    @GetMapping("/{portfolioId}/edit")
    public String editPortfolioForm(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            Model model
    ) {
        PortfolioDto portfolioDto = portfolioService.getPortfolioDto(photographerId, portfolioId);
        model.addAttribute("portfolioDto", portfolioDto);
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("portfolioId", portfolioId);
        model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/" + portfolioId + "/edit");
        return "photographer/editPortfolio";
    }

    @PostMapping("/{portfolioId}/edit")
    public String editPortfolio(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            @ModelAttribute("portfolioDto") PortfolioDto portfolioDto,
            Model model
    ) {
        try {
            portfolioService.updatePortfolio(photographerId, portfolioId, portfolioDto);
            return "redirect:/photographer/" + photographerId + "/portfolio/" + portfolioId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("portfolioDto", portfolioDto);
            model.addAttribute("photographerId", photographerId);
            model.addAttribute("portfolioId", portfolioId);
            model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/" + portfolioId + "/edit");
            return "photographer/editPortfolio";
        }
    }
}