package com.pickkasso.pickkasso.global.controller;

import com.pickkasso.pickkasso.global.dto.AiPickResultDto;
import com.pickkasso.pickkasso.global.dto.ChatMessageDto;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AiPickController {

    private static final String PENDING_EXPANSION_QUERY = "aiPickPendingExpansionQuery";
    private static final String PENDING_DETAIL_QUERY = "aiPickPendingDetailQuery";
    private static final String CHAT_HISTORY = "aiPickChatHistory";
    private static final String LAST_SEARCH_QUERY = "aiPickLastSearchQuery";
    private static final String LAST_EXPAND_NEARBY = "aiPickLastExpandNearby";
    private static final String EXCLUDED_RECOMMENDATION_IDS = "aiPickExcludedRecommendationIds";

    private final AiPickRecommendationService aiPickRecommendationService;

    @GetMapping("/ai-pick")
    public String aiPick(Model model, HttpSession session) {
        session.removeAttribute(PENDING_EXPANSION_QUERY);
        session.removeAttribute(PENDING_DETAIL_QUERY);
        session.removeAttribute(CHAT_HISTORY);
        session.removeAttribute(LAST_SEARCH_QUERY);
        session.removeAttribute(LAST_EXPAND_NEARBY);
        session.removeAttribute(EXCLUDED_RECOMMENDATION_IDS);
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
                         @RequestParam(name = "reroll", required = false, defaultValue = "false") boolean reroll,
                         HttpSession session,
                         Model model) {
        SearchResultPayload payload = processSearch(query, expandNearby, reroll, session);
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
                                          @RequestParam(name = "reroll", required = false, defaultValue = "false") boolean reroll,
                                          HttpSession session) {
        SearchResultPayload payload = processSearch(query, expandNearby, reroll, session);
        AiPickResultDto result = payload.result();
        return Map.of(
            "query", payload.rawQuery(),
            "effectiveQuery", payload.effectiveQuery(),
            "messages", result.messages(),
            "recommendations", result.recommendations(),
            "needsNearbyExpansionConfirm", result.needsNearbyExpansionConfirm(),
            "suggestedNearbyLabel", result.suggestedNearbyLabel()
        );
    }

    private SearchResultPayload processSearch(String query, boolean expandNearby, boolean reroll, HttpSession session) {
        String rawQuery = query == null ? "" : query.trim();
        String searchQuery = rawQuery;
        boolean useNearbyExpansion = expandNearby;
        boolean confirmedByUserAnswer = false;
        boolean forceRecommendWithCurrentInfo = false;
        List<ChatMessageDto> history = getHistory(session);
        Set<Long> excludedIds = getExcludedIds(session);

        if (reroll) {
            String lastQuery = (String) session.getAttribute(LAST_SEARCH_QUERY);
            if (lastQuery != null && !lastQuery.isBlank()) {
                searchQuery = lastQuery;
            }
            Object savedExpand = session.getAttribute(LAST_EXPAND_NEARBY);
            if (savedExpand instanceof Boolean saved) {
                useNearbyExpansion = saved;
            }
            // 다시 추천은 기존 확정 조건으로 결과만 재추출해야 하므로
            // 조건 보완/범위확장 대기 상태는 무시한다.
            session.removeAttribute(PENDING_DETAIL_QUERY);
            session.removeAttribute(PENDING_EXPANSION_QUERY);
        } else if (rawQuery != null && !rawQuery.isBlank()) {
            history.add(new ChatMessageDto("user", rawQuery));
            excludedIds.clear();
            session.setAttribute(EXCLUDED_RECOMMENDATION_IDS, excludedIds);
        }

        String pendingDetailQuery = (String) session.getAttribute(PENDING_DETAIL_QUERY);

        // 버튼이 안 보이거나 사용자가 직접 "네"라고 답한 경우도 인접지역 확장을 허용
        if (isNearbyExpansionConfirmAnswer(rawQuery)) {
            String pendingQuery = (String) session.getAttribute(PENDING_EXPANSION_QUERY);
            if (pendingQuery != null && !pendingQuery.isBlank()) {
                searchQuery = pendingQuery;
                useNearbyExpansion = true;
                confirmedByUserAnswer = true;
            }
        }

        // "이 정도로만 추천해줘" 요청이면 필수조건 미충족이어도 현재 정보로 바로 추천 진행
        if (!confirmedByUserAnswer && isRecommendAsIsAnswer(rawQuery)) {
            if (pendingDetailQuery != null && !pendingDetailQuery.isBlank()) {
                searchQuery = pendingDetailQuery;
            }
            forceRecommendWithCurrentInfo = true;
        }

        // 조건 보완 단계에서 사용자가 추가 입력을 주면 기존 질의와 합쳐서 재검색
        if (!confirmedByUserAnswer
            && !forceRecommendWithCurrentInfo
            && pendingDetailQuery != null
            && !pendingDetailQuery.isBlank()
            && rawQuery != null
            && !rawQuery.isBlank()) {
            searchQuery = pendingDetailQuery + " " + rawQuery;
        }

        AiPickResultDto baseResult = aiPickRecommendationService.recommend(searchQuery, useNearbyExpansion, excludedIds);
        AiPickResultDto result = baseResult;

        if (!reroll && !confirmedByUserAnswer && !forceRecommendWithCurrentInfo && shouldAskForMoreConditions(baseResult)) {
            history.add(new ChatMessageDto("ai", buildMissingConditionMessage(baseResult)));

            result = new AiPickResultDto(
                baseResult.parsedQuery(),
                new ArrayList<>(history),
                List.of(),
                false,
                ""
            );
            // 조건 부족 단계에서는 범위 확장 플로우를 태우지 않는다.
            session.removeAttribute(PENDING_EXPANSION_QUERY);
            session.setAttribute(PENDING_DETAIL_QUERY, searchQuery);
            session.setAttribute(CHAT_HISTORY, history);
            return new SearchResultPayload(rawQuery, searchQuery, result);
        } else {
            session.removeAttribute(PENDING_DETAIL_QUERY);
        }

        if (reroll) {
            result = new AiPickResultDto(
                baseResult.parsedQuery(),
                new ArrayList<>(history),
                baseResult.recommendations(),
                baseResult.needsNearbyExpansionConfirm(),
                baseResult.suggestedNearbyLabel()
            );
        } else if (confirmedByUserAnswer) {
            history.add(new ChatMessageDto("ai", "좋아요! 범위를 확장해서 추천해드릴게요."));
            appendAiMessagesOnly(baseResult, history);
            result = new AiPickResultDto(
                baseResult.parsedQuery(),
                new ArrayList<>(history),
                baseResult.recommendations(),
                baseResult.needsNearbyExpansionConfirm(),
                baseResult.suggestedNearbyLabel()
            );
        } else {
            appendAiMessagesOnly(baseResult, history);
            result = new AiPickResultDto(
                baseResult.parsedQuery(),
                new ArrayList<>(history),
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
        session.setAttribute(LAST_SEARCH_QUERY, searchQuery);
        session.setAttribute(LAST_EXPAND_NEARBY, useNearbyExpansion);
        for (var rec : result.recommendations()) {
            if (rec != null && rec.itemId() != null) {
                excludedIds.add(rec.itemId());
            }
        }
        session.setAttribute(EXCLUDED_RECOMMENDATION_IDS, excludedIds);
        session.setAttribute(CHAT_HISTORY, history);
        return new SearchResultPayload(rawQuery, searchQuery, result);
    }

    private boolean isNearbyExpansionConfirmAnswer(String query) {
        if (query == null) return false;
        String normalized = query.replaceAll("\\s+", "").toLowerCase();
        return normalized.equals("네")
            || normalized.equals("예")
            || normalized.equals("네좋아요")
            || normalized.equals("좋아요")
            || normalized.equals("응")
            || normalized.contains("추천해줘")
            || normalized.contains("추천해주세요")
            || normalized.contains("추천해줘요")
            || normalized.contains("추천부탁")
            || normalized.contains("추천부탁해")
            || normalized.contains("추천부탁해요");
    }

    private boolean isRecommendAsIsAnswer(String query) {
        if (query == null) return false;
        String normalized = query.replaceAll("\\s+", "").toLowerCase();
        return normalized.contains("이대로추천")
            || normalized.contains("바로추천")
            || normalized.contains("바로추천해줘")
            || normalized.contains("바로보여줘")
            || normalized.contains("이정도로")
            || normalized.contains("이정도만")
            || normalized.contains("그냥추천")
            || normalized.contains("걍추천")
            || normalized.contains("더안채울")
            || normalized.contains("채우기싫")
            || normalized.contains("조건없이추천");
    }

    /**
     * 조건 부족 판단 규칙(단일 기준):
     * 카테고리/지역/예산/날짜/스타일이 모두 있어야 추천 진행.
     */
    private boolean shouldAskForMoreConditions(AiPickResultDto result) {
        if (result == null || result.parsedQuery() == null) {
            return true;
        }
        var p = result.parsedQuery();
        boolean hasCategory = p.category() != null && !p.category().isBlank();
        boolean hasLocation = p.location() != null && !p.location().isBlank();
        boolean hasBudget = p.maxPrice() != null;
        boolean hasDate = p.requestedDate() != null && !p.requestedDate().isBlank();
        boolean hasStyle = p.styleTags() != null && !p.styleTags().isEmpty();
        return !(hasCategory && hasLocation && hasBudget && hasDate && hasStyle);
    }

    private String buildMissingConditionMessage(AiPickResultDto result) {
        var p = result.parsedQuery();
        List<String> missing = new ArrayList<>();
        if (p.category() == null || p.category().isBlank()) missing.add("촬영 종류");
        if (p.location() == null || p.location().isBlank()) missing.add("지역");
        if (p.maxPrice() == null) missing.add("예산");
        if (p.requestedDate() == null || p.requestedDate().isBlank()) missing.add("날짜");
        if (p.styleTags() == null || p.styleTags().isEmpty()) missing.add("스타일");

        String missingText = missing.isEmpty() ? "조건" : String.join(", ", missing);
        return "조건이 조금 부족해요.<br>"
            + "<span class='text-[#1D3CFF] font-bold'>" + missingText + "</span> 조건을 더 알려주시면 추천해드릴게요.<br>"
            + "필수 조건이 모두 채워지면 바로 결과를 보여드릴게요.<br>"
            + "<span class='text-[#1D3CFF] font-bold'>예시)</span> "
            + "\"다음주 토요일 강남에서 10만원대 자연스러운 커플 스냅\" 또는 "
            + "\"바로 추천해줘\"";
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessageDto> getHistory(HttpSession session) {
        Object saved = session.getAttribute(CHAT_HISTORY);
        if (saved instanceof List<?>) {
            return new ArrayList<>((List<ChatMessageDto>) saved);
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private Set<Long> getExcludedIds(HttpSession session) {
        Object saved = session.getAttribute(EXCLUDED_RECOMMENDATION_IDS);
        if (saved instanceof Set<?>) {
            return new LinkedHashSet<>((Set<Long>) saved);
        }
        return new LinkedHashSet<>();
    }

    private void appendAiMessagesOnly(AiPickResultDto result, List<ChatMessageDto> target) {
        if (result == null || result.messages() == null || target == null) {
            return;
        }
        for (ChatMessageDto message : result.messages()) {
            if (message != null && "ai".equals(message.role())) {
                target.add(message);
            }
        }
    }

    private record SearchResultPayload(String rawQuery, String effectiveQuery, AiPickResultDto result) {}
}
