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
}