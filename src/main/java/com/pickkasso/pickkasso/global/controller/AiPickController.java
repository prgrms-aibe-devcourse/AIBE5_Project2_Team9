package com.pickkasso.pickkasso.global.controller;

import com.pickkasso.pickkasso.global.dto.aipick.AiPickResultDto;
import com.pickkasso.pickkasso.global.dto.aipick.ChatMessageDto;
import com.pickkasso.pickkasso.global.service.AiPickRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AiPickController {

    private static final String PENDING_EXPANSION_QUERY = "aiPickPendingExpansionQuery";

    private final AiPickRecommendationService aiPickRecommendationService;

    @GetMapping("/ai-pick")
    public String aiPick(Model model) {
        model.addAttribute("query", "");
        model.addAttribute("messages", java.util.List.of());
        model.addAttribute("recommendations", java.util.List.of());
        model.addAttribute("needsNearbyExpansionConfirm", false);
        model.addAttribute("suggestedNearbyLabel", "");
        return "common/ai-pick";
    }

    @PostMapping("/ai-pick/search")
    public String search(@RequestParam(name = "query", required = false) String query,
                         @RequestParam(name = "expandNearby", required = false, defaultValue = "false") boolean expandNearby,
                         HttpSession session,
                         Model model) {
        SearchResultPayload payload = processSearch(query, expandNearby, session);
        AiPickResultDto result = payload.result();

        model.addAttribute("query", payload.rawQuery());
        model.addAttribute("messages", result.messages());
        model.addAttribute("recommendations", result.recommendations());
        model.addAttribute("needsNearbyExpansionConfirm", result.needsNearbyExpansionConfirm());
        model.addAttribute("suggestedNearbyLabel", result.suggestedNearbyLabel());
        return "common/ai-pick";
    }

    @PostMapping("/ai-pick/search-ajax")
    @ResponseBody
    public Map<String, Object> searchAjax(@RequestParam(name = "query", required = false) String query,
                                          @RequestParam(name = "expandNearby", required = false, defaultValue = "false") boolean expandNearby,
                                          HttpSession session) {
        SearchResultPayload payload = processSearch(query, expandNearby, session);
        AiPickResultDto result = payload.result();
        return Map.of(
            "query", payload.rawQuery(),
            "messages", result.messages(),
            "recommendations", result.recommendations(),
            "needsNearbyExpansionConfirm", result.needsNearbyExpansionConfirm(),
            "suggestedNearbyLabel", result.suggestedNearbyLabel()
        );
    }

    private SearchResultPayload processSearch(String query, boolean expandNearby, HttpSession session) {
        String rawQuery = query == null ? "" : query.trim();
        String searchQuery = rawQuery;
        boolean useNearbyExpansion = expandNearby;
        boolean confirmedByUserAnswer = false;

        // 버튼이 안 보이거나 사용자가 직접 "네"라고 답한 경우도 인접지역 확장을 허용
        if (isNearbyExpansionConfirmAnswer(rawQuery)) {
            String pendingQuery = (String) session.getAttribute(PENDING_EXPANSION_QUERY);
            if (pendingQuery != null && !pendingQuery.isBlank()) {
                searchQuery = pendingQuery;
                useNearbyExpansion = true;
                confirmedByUserAnswer = true;
            }
        }

        AiPickResultDto baseResult = aiPickRecommendationService.recommend(searchQuery, useNearbyExpansion);
        AiPickResultDto result = baseResult;

        if (confirmedByUserAnswer) {
            List<ChatMessageDto> mergedMessages = new ArrayList<>();
            List<ChatMessageDto> current = baseResult.messages() == null ? List.of() : baseResult.messages();
            if (!current.isEmpty()) {
                mergedMessages.add(current.get(0)); // 원래 사용자 질문
            }
            mergedMessages.add(new ChatMessageDto("ai", "해당 조건으로는 결과가 없어요.<br>범위를 확장해서 추천해드릴까요?"));
            mergedMessages.add(new ChatMessageDto("user", rawQuery));
            mergedMessages.add(new ChatMessageDto("ai", "좋아요! 범위를 확장해서 추천해드릴게요."));
            if (current.size() > 1) {
                mergedMessages.addAll(current.subList(1, current.size()));
            }
            result = new AiPickResultDto(
                baseResult.parsedQuery(),
                mergedMessages,
                baseResult.recommendations(),
                baseResult.needsNearbyExpansionConfirm(),
                baseResult.suggestedNearbyLabel()
            );
        }

        if (result.needsNearbyExpansionConfirm()) {
            session.setAttribute(PENDING_EXPANSION_QUERY, searchQuery);
        } else {
            session.removeAttribute(PENDING_EXPANSION_QUERY);
        }
        return new SearchResultPayload(rawQuery, result);
    }

    private boolean isNearbyExpansionConfirmAnswer(String query) {
        if (query == null) return false;
        String normalized = query.replaceAll("\\s+", "").toLowerCase();
        return normalized.equals("네")
            || normalized.equals("예")
            || normalized.equals("네좋아요")
            || normalized.equals("좋아요")
            || normalized.equals("응");
    }

    private record SearchResultPayload(String rawQuery, AiPickResultDto result) {}
}
