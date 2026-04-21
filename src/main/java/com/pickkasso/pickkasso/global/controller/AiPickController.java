package com.pickkasso.pickkasso.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AiPickController {

    // TODO(backend): AiRecommendationService 주입 후 세션/히스토리 기반 데이터로 채울 것
    //   model attributes:
    //     - messages        : List<ChatMessageDto>       { role: "ai"|"user", content: String(HTML 허용) }
    //     - recommendations : List<AiRecommendationDto>  { photographerId, category, title, photographerName,
    //                                                      rating, reviewCount, region, priceFrom, reason, thumbUrl }
    //     - query           : String (textarea 재입력 유지용)

    @GetMapping("/ai-pick")
    public String aiPick(Model model) {
        model.addAttribute("query", "");
        return "common/ai-pick";
    }

    @PostMapping("/ai-pick/search")
    public String search(@RequestParam(name = "query", required = false) String query,
                         Model model) {
        model.addAttribute("query", query == null ? "" : query);
        return "common/ai-pick";
    }
}
