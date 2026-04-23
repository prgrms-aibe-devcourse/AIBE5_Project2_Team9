package com.pickkasso.pickkasso.global.service;

import com.pickkasso.pickkasso.global.dto.aipick.AiPickQueryDto;
import com.pickkasso.pickkasso.global.dto.aipick.AiPickResultDto;
import com.pickkasso.pickkasso.global.dto.aipick.AiRecommendationDto;
import com.pickkasso.pickkasso.global.dto.aipick.ChatMessageDto;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPickRecommendationService {

    private static final int RECOMMENDATION_COUNT = 3;
    private static final Pattern MANWON_PATTERN = Pattern.compile("(\\d+)\\s*만\\s*원");
    private static final Pattern WON_PATTERN = Pattern.compile("(\\d{5,9})\\s*원");
    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2}[./-]\\d{1,2}[./-]\\d{1,2})");
    private static final Map<String, List<String>> NEARBY_REGIONS = Map.of(
        "홍대", List.of("홍대", "연남", "합정", "상수", "망원", "마포"),
        "강남", List.of("강남", "신논현", "역삼", "선릉", "삼성"),
        "성수", List.of("성수", "뚝섬", "서울숲", "건대"),
        "잠실", List.of("잠실", "송파", "석촌", "강동")
    );

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiApiUrl;

    public AiPickResultDto recommend(String rawQuery, boolean expandNearby) {
        AiPickQueryDto parsed = parseQueryWithGeminiOrRule(rawQuery);
        List<Item> candidates = itemRepository.findScoreItemList(PageRequest.of(0, 60));
        List<ScoredItem> ranked = rankCandidates(candidates, parsed, expandNearby);
        List<AiRecommendationDto> recommendations = mapRecommendations(ranked, parsed);
        boolean needsExpandConfirm = !expandNearby && recommendations.isEmpty() && parsed.location() != null;
        List<ChatMessageDto> messages = buildMessages(parsed, recommendations, needsExpandConfirm);
        return new AiPickResultDto(parsed, messages, recommendations, needsExpandConfirm, buildNearbyLabel(parsed.location()));
    }

    private AiPickQueryDto parseQueryWithGeminiOrRule(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.isBlank()) {
            return new AiPickQueryDto("", null, null, null, null, List.of());
        }
        AiPickQueryDto ruleParsed = parseQueryByRule(query);
        AiPickQueryDto geminiParsed = parseQueryWithGemini(query);
        if (geminiParsed == null) {
            return ruleParsed;
        }
        return mergeParsedQuery(geminiParsed, ruleParsed);
    }

    private List<ScoredItem> rankCandidates(List<Item> candidates, AiPickQueryDto parsed, boolean expandNearby) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<Item> base = new ArrayList<>(candidates);

        if (parsed.category() != null) {
            List<Item> categoryMatched = base.stream()
                .filter(item -> isCategoryTagMatch(item, parsed.category()))
                .toList();
            if (!categoryMatched.isEmpty()) {
                base = new ArrayList<>(categoryMatched);
            } else {
                List<Item> softCategoryMatched = base.stream()
                    .filter(item -> isCategorySoftMatch(item, parsed.category()))
                    .toList();
                if (!softCategoryMatched.isEmpty()) {
                    base = new ArrayList<>(softCategoryMatched);
                }
            }
        }

        if (parsed.location() != null) {
            List<Item> strictLocationMatched = base.stream()
                .filter(item -> hasText(item.getAddress()) && item.getAddress().contains(parsed.location()))
                .toList();
            if (!strictLocationMatched.isEmpty()) {
                base = new ArrayList<>(strictLocationMatched);
            } else if (expandNearby) {
                // 사용자가 동의했을 때만 인접 지역으로 제한적으로 확장
                List<String> nearKeywords = nearbyRegionKeywords(parsed.location());
                List<Item> nearbyMatched = base.stream()
                    .filter(item -> matchesAnyRegion(item.getAddress(), nearKeywords))
                    .toList();
                if (!nearbyMatched.isEmpty()) {
                    base = new ArrayList<>(nearbyMatched);
                }
            } else {
                return List.of();
            }
        }

        if (parsed.maxPrice() != null) {
            List<Item> withinBudget = base.stream()
                .filter(item -> item.getDefaultPrice() <= parsed.maxPrice())
                .toList();
            if (!withinBudget.isEmpty()) {
                base = new ArrayList<>(withinBudget);
            } else {
                int relaxedBudget = (int) (parsed.maxPrice() * 1.3);
                List<Item> relaxed = base.stream()
                    .filter(item -> item.getDefaultPrice() <= relaxedBudget)
                    .toList();
                if (!relaxed.isEmpty()) {
                    base = new ArrayList<>(relaxed);
                }
            }
        }

        List<ScoredItem> scored = base.stream()
            .map(item -> new ScoredItem(item, calculateMatchScore(item, parsed), ""))
            .sorted(Comparator
                .comparingInt(ScoredItem::score).reversed()
                .thenComparing(Comparator.comparingInt((ScoredItem si) -> si.item().getAvgScore()).reversed())
                .thenComparing(Comparator.comparingInt((ScoredItem si) -> si.item().getReviewCount()).reversed()))
            .toList();

        List<ScoredItem> top = scored.stream().limit(RECOMMENDATION_COUNT).toList();
        List<ScoredItem> withReason = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            ScoredItem now = top.get(i);
            withReason.add(new ScoredItem(now.item(), now.score(), buildReason(parsed, now.item(), i + 1)));
        }
        return withReason;
    }

    private List<AiRecommendationDto> mapRecommendations(List<ScoredItem> scoredItems, AiPickQueryDto parsed) {
        if (scoredItems == null || scoredItems.isEmpty()) {
            return List.of();
        }

        List<AiRecommendationDto> mapped = new ArrayList<>();
        for (ScoredItem scored : scoredItems) {
            Item item = scored.item();
            mapped.add(new AiRecommendationDto(
                item.getId(),
                item.getPhotographer().getId(),
                item.getTag().getName(),
                item.getName(),
                item.getPhotographer().getName(),
                item.getAvgScore() / 100.0,
                item.getReviewCount(),
                extractRegion(item.getAddress(), parsed.location()),
                item.getDefaultPrice(),
                scored.reason(),
                null
            ));
        }
        return mapped;
    }

    private List<ChatMessageDto> buildMessages(
        AiPickQueryDto parsed,
        List<AiRecommendationDto> recommendations,
        boolean needsExpandConfirm
    ) {
        List<ChatMessageDto> messages = new ArrayList<>();
        if (parsed.rawQuery() != null && !parsed.rawQuery().isBlank()) {
            messages.add(new ChatMessageDto("user", parsed.rawQuery()));
        }

        String dateText = parsed.requestedDate() == null ? "날짜 조건 미지정" : parsed.requestedDate();
        String locationText = parsed.location() == null ? "지역 조건 미지정" : parsed.location();
        String priceText = parsed.maxPrice() == null ? "예산 조건 미지정" : String.format("%,d원 이내", parsed.maxPrice());
        String categoryText = parsed.category() == null ? "카테고리 미지정" : parsed.category();
        String styleText = (parsed.styleTags() == null || parsed.styleTags().isEmpty())
            ? "스타일 조건 미지정"
            : String.join(", ", parsed.styleTags());

        String summary;
        if (needsExpandConfirm) {
            summary = "<span class='text-[#1D3CFF] font-bold'>조건을 확인했어요.</span><br>"
                + "📍 " + locationText
                + " · 📅 " + dateText
                + " · 💰 " + priceText
                + " · 📸 " + categoryText
                + " · 🎨 " + styleText;
            messages.add(new ChatMessageDto("ai", summary));
            messages.add(new ChatMessageDto("ai", "해당 조건으로는 결과가 없어요.<br>범위를 확장해서 추천해드릴까요?"));
        } else {
            summary = "<span class='text-[#1D3CFF] font-bold'>조건을 확인했어요!</span><br>"
                + "📍 " + locationText
                + " · 📅 " + dateText
                + " · 💰 " + priceText
                + " · 📸 " + categoryText
                + " · 🎨 " + styleText
                + "<br><br>조건에 맞는 작가 " + recommendations.size() + "명을 찾았습니다 👇";
            messages.add(new ChatMessageDto("ai", summary));
        }

        return messages;
    }

    private AiPickQueryDto parseQueryByRule(String query) {
        String date = parseDate(query);
        Integer maxPrice = parsePrice(query);
        String location = parseLocation(query);
        String category = parseCategory(query);
        List<String> styleTags = parseStyleTags(query);

        return new AiPickQueryDto(query, date, location, maxPrice, category, styleTags);
    }

    private AiPickQueryDto parseQueryWithGemini(String query) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return null;
        }

        String endpoint = geminiApiUrl + "/models/" + geminiModel + ":generateContent";
        String prompt = """
            사용자의 촬영 요청 문장에서 조건을 JSON으로만 추출해 주세요.
            반드시 JSON 객체만 반환하고, 설명/마크다운/code fence는 절대 포함하지 마세요.

            스키마:
            {
              "date": string | null,
              "location": string | null,
              "max_price": number | null,
              "category": string | null,
              "style_tags": string[]
            }

            규칙:
            - max_price는 숫자(원 단위 정수)만 반환
            - date는 가능하면 YYYY-MM-DD 형식 문자열로 반환
            - 추출 불가한 값은 null
            - style_tags는 없으면 빈 배열

            사용자 입력:
            """ + query;

        String requestBody = """
            {
              "contents": [
                {
                  "parts": [
                    {"text": %s}
                  ]
                }
              ]
            }
            """.formatted(toJsonString(prompt));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            String modelText = extractModelText(response.body());
            if (modelText == null || modelText.isBlank()) {
                return null;
            }
            return parseGeminiJson(query, modelText);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractModelText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }

        String text = parts.get(0).path("text").asText(null);
        if (text == null) return null;
        return stripCodeFence(text.trim());
    }

    private AiPickQueryDto parseGeminiJson(String rawQuery, String jsonText) throws Exception {
        JsonNode root = objectMapper.readTree(jsonText);

        String date = asNullableText(root.path("date"));
        String location = asNullableText(root.path("location"));
        String category = asNullableText(root.path("category"));
        Integer maxPrice = asNullableInt(root.path("max_price"));
        List<String> styleTags = new ArrayList<>();

        JsonNode styleArray = root.path("style_tags");
        if (styleArray.isArray()) {
            for (JsonNode node : styleArray) {
                String value = asNullableText(node);
                if (value != null) {
                    styleTags.add(value);
                }
            }
        }
        return new AiPickQueryDto(rawQuery, date, location, maxPrice, category, styleTags);
    }

    private String stripCodeFence(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return text.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return text;
    }

    private String asNullableText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String value = node.asText();
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value) ? null : value;
    }

    private Integer asNullableInt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        if (node.isInt() || node.isLong()) return node.asInt();
        if (node.isTextual()) {
            String value = node.asText().replaceAll("[^0-9]", "");
            if (!value.isBlank()) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }

    private Integer parsePrice(String query) {
        Matcher manwonMatcher = MANWON_PATTERN.matcher(query);
        if (manwonMatcher.find()) {
            return Integer.parseInt(manwonMatcher.group(1)) * 10_000;
        }
        Matcher wonMatcher = WON_PATTERN.matcher(query);
        if (wonMatcher.find()) {
            return Integer.parseInt(wonMatcher.group(1));
        }
        return null;
    }

    private String parseDate(String query) {
        Matcher dateMatcher = DATE_PATTERN.matcher(query);
        if (dateMatcher.find()) {
            return dateMatcher.group(1).replace('.', '-').replace('/', '-');
        }
        if (query.contains("다음 주 토요일")) return "다음 주 토요일";
        if (query.contains("다음 주 일요일")) return "다음 주 일요일";
        if (query.contains("이번 주 토요일")) return "이번 주 토요일";
        if (query.contains("이번 주 일요일")) return "이번 주 일요일";
        if (query.contains("토요일")) return "토요일";
        if (query.contains("일요일")) return "일요일";
        return null;
    }

    private String parseLocation(String query) {
        String[] regions = {"홍대", "연남", "합정", "마포", "강남", "성수", "잠실", "서울", "부산", "제주"};
        for (String region : regions) {
            if (query.contains(region)) {
                return region;
            }
        }
        return null;
    }

    private String parseCategory(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        if (normalized.contains("데이트") || normalized.contains("커플")) return "데이트";
        if (normalized.contains("웨딩")) return "웨딩";
        if (normalized.contains("가족")) return "가족";
        if (normalized.contains("프로필")) return "프로필";
        if (normalized.contains("제품")) return "제품";
        return null;
    }

    private List<String> parseStyleTags(String query) {
        List<String> styles = new ArrayList<>();
        if (query.contains("감성")) styles.add("감성");
        if (query.contains("자연")) styles.add("자연스러운");
        if (query.contains("깔끔")) styles.add("깔끔한");
        if (query.contains("빈티지")) styles.add("빈티지");
        return styles;
    }

    private String buildReason(AiPickQueryDto parsed, Item item, int rank) {
        boolean categoryMatched = parsed.category() != null && isCategorySoftMatch(item, parsed.category());
        boolean locationMatched = parsed.location() != null && hasText(item.getAddress()) && item.getAddress().contains(parsed.location());
        boolean budgetMatched = parsed.maxPrice() != null && item.getDefaultPrice() <= parsed.maxPrice();
        boolean budgetNear = parsed.maxPrice() != null && !budgetMatched && item.getDefaultPrice() <= parsed.maxPrice() * 1.3;
        String region = extractRegion(item.getAddress(), parsed.location());
        String stylePhrase = pickStylePhrase(parsed.styleTags());
        String productSignal = buildDataPoint(item);

        String opener;
        if (locationMatched) {
            opener = region + " 근처에서 찍기 좋고";
        } else if (categoryMatched && parsed.category() != null) {
            opener = "전체 분위기가 " + parsed.category() + " 촬영에 잘 어울리고";
        } else {
            opener = "전체 촬영 톤이 깔끔하고";
        }

        String stylePart;
        if (stylePhrase != null) {
            stylePart = stylePhrase + " 분위기를 선호하실 때 잘 어울려요";
        } else {
            stylePart = "촬영 톤이 자연스럽게 어울려요";
        }

        String budgetPart = budgetMatched
            ? "예산 범위에도 맞아요"
            : (budgetNear ? "예산을 조금 넘지만 구성 대비 만족도가 높아요" : "가격 대비 구성이 안정적인 편이에요");

        String rankTail = rank == 1
            ? "우선 추천드려요"
            : (rank == 2 ? "두 번째 후보로도 반응이 좋아요" : "가볍게 같이 비교해보시기 좋아요");

        // 한 문장으로만 노출
        return opener + " " + productSignal + " " + stylePart + ", " + budgetPart + " " + rankTail + ".";
    }

    private String pickStylePhrase(List<String> styleTags) {
        if (styleTags == null || styleTags.isEmpty()) return null;
        String first = styleTags.get(0);
        if ("감성".equals(first)) return "감성적인";
        if ("자연스러운".equals(first)) return "자연스러운";
        if ("깔끔한".equals(first)) return "깔끔한";
        if ("빈티지".equals(first)) return "빈티지한";
        return first;
    }

    private String buildDataPoint(Item item) {
        String summary = summarizeDescription(item.getDescription());
        if (summary != null) {
            return "'" + summary + "' 포인트가 잘 살아 있고";
        }

        if (item.getPlanList() != null && !item.getPlanList().isEmpty()) {
            Plan cheapest = item.getPlanList().stream()
                .min(Comparator.comparingInt(Plan::getPrice))
                .orElse(null);
            if (cheapest != null) {
                Integer duration = cheapest.getShootingDuration();
                Integer edited = cheapest.getEditedPhotoCount();
                Integer delivery = cheapest.getDeliveryDays();
                if (duration != null && edited != null && delivery != null) {
                    return "최저가 플랜에 " + duration + "시간 촬영과 보정본 " + edited + "장 구성이 포함되어";
                }
                if (duration != null && delivery != null) {
                    return "최저가 플랜에 " + duration + "시간 촬영 구성이 들어가 있어";
                }
            }
        }

        if (hasText(item.getIncludes())) {
            return "포함 서비스 안내가 분명해서";
        }
        if (hasText(item.getExcludes())) {
            return "제외 항목이 깔끔하게 정리돼 있어";
        }
        return "상품 정보가 안정적으로 정리되어";
    }

    private String summarizeDescription(String description) {
        if (!hasText(description)) return null;
        String normalized = description.replaceAll("\\s+", " ").trim();
        if (normalized.length() < 8) return null;

        String[] split = normalized.split("[.!?]|\\n");
        String first = split.length > 0 ? split[0].trim() : normalized;
        if (first.isBlank()) return null;

        if (first.length() > 32) {
            first = first.substring(0, 32).trim() + "...";
        }
        return first;
    }

    private String extractRegion(String address, String fallback) {
        if (address == null || address.isBlank()) return fallback == null ? "지역 정보 없음" : fallback;
        String[] parts = address.split("\\s+");
        if (parts.length >= 2) return parts[0] + " " + parts[1];
        return address;
    }

    private int calculateMatchScore(Item item, AiPickQueryDto parsed) {
        int score = 0;
        if (parsed.category() != null && isCategorySoftMatch(item, parsed.category())) {
            score += 50;
        }
        if (parsed.location() != null && hasText(item.getAddress()) && item.getAddress().contains(parsed.location())) {
            score += 20;
        }
        if (parsed.maxPrice() != null) {
            if (item.getDefaultPrice() <= parsed.maxPrice()) score += 20;
            else if (item.getDefaultPrice() <= parsed.maxPrice() * 1.3) score += 10;
        }
        score += Math.min(item.getReviewCount(), 30);
        score += Math.min(item.getAvgScore() / 20, 10);
        return score;
    }

    private boolean isCategoryTagMatch(Item item, String category) {
        List<String> keywords = categoryKeywords(category);
        String tag = safeLower(item.getTag().getName());
        for (String keyword : keywords) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            if (tag.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCategorySoftMatch(Item item, String category) {
        List<String> keywords = categoryKeywords(category);
        String tag = safeLower(item.getTag().getName());
        String name = safeLower(item.getName());
        String description = safeLower(item.getDescription());

        for (String keyword : keywords) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            if (tag.contains(kw) || name.contains(kw) || description.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private List<String> categoryKeywords(String category) {
        if (category == null) return List.of();
        String value = category.toLowerCase(Locale.ROOT);
        if (value.contains("웨딩")) return List.of("웨딩", "본식", "스드메", "예식");
        if (value.contains("데이트") || value.contains("커플")) return List.of("데이트", "커플", "스냅");
        if (value.contains("가족")) return List.of("가족", "패밀리", "베이비", "아이");
        if (value.contains("프로필")) return List.of("프로필", "증명", "취업");
        if (value.contains("제품")) return List.of("제품", "상품", "브랜딩");
        return List.of(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<String> nearbyRegionKeywords(String location) {
        if (location == null || location.isBlank()) return List.of();
        for (Map.Entry<String, List<String>> entry : NEARBY_REGIONS.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return List.of(location);
    }

    private boolean matchesAnyRegion(String address, List<String> keywords) {
        if (!hasText(address) || keywords == null || keywords.isEmpty()) return false;
        for (String keyword : keywords) {
            if (address.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildNearbyLabel(String location) {
        List<String> nearby = nearbyRegionKeywords(location);
        if (nearby.isEmpty()) {
            return "인접 지역";
        }
        return String.join(", ", nearby);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private AiPickQueryDto mergeParsedQuery(AiPickQueryDto primary, AiPickQueryDto fallback) {
        String requestedDate = primary.requestedDate() != null ? primary.requestedDate() : fallback.requestedDate();
        String location = primary.location() != null ? primary.location() : fallback.location();
        Integer maxPrice = primary.maxPrice() != null ? primary.maxPrice() : fallback.maxPrice();
        String category = primary.category() != null ? primary.category() : fallback.category();
        List<String> styleTags = (primary.styleTags() != null && !primary.styleTags().isEmpty())
            ? primary.styleTags()
            : fallback.styleTags();
        return new AiPickQueryDto(primary.rawQuery(), requestedDate, location, maxPrice, category, styleTags);
    }

    private record ScoredItem(Item item, int score, String reason) {}
}
