package com.pickkasso.pickkasso.global.service;

import com.pickkasso.pickkasso.global.dto.AiPickQueryDto;
import com.pickkasso.pickkasso.global.dto.AiPickResultDto;
import com.pickkasso.pickkasso.global.dto.AiRecommendationDto;
import com.pickkasso.pickkasso.global.dto.ChatMessageDto;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.repository.AiItemQuerySpec;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPickRecommendationService {

    private static final int RECOMMENDATION_COUNT = 3;
    private static final int AI_PICK_CANDIDATE_DB_LIMIT = 150;
    private static final int AI_PICK_GLOBAL_FALLBACK_LIMIT = 80;
    private static final Pattern MANWON_PATTERN = Pattern.compile("(\\d+)\\s*만\\s*원");
    private static final Pattern WON_PATTERN = Pattern.compile("(\\d{5,9})\\s*원");
    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2}[./-]\\d{1,2}[./-]\\d{1,2})");
    private static final Pattern MONTH_DAY_KO_PATTERN = Pattern.compile("(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일");
    private static final Pattern MONTH_DAY_SLASH_PATTERN = Pattern.compile("(\\d{1,2})\\s*/\\s*(\\d{1,2})(?:\\s*일)?");
    private static final Pattern MONTH_DAY_DASH_PATTERN = Pattern.compile("(\\d{1,2})\\s*-\\s*(\\d{1,2})(?:\\s*일)?");
    private static final Pattern HOUR_PATTERN = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})\\s*시");
    private static final Pattern HOUR_COLON_PATTERN = Pattern.compile("\\b(\\d{1,2})\\s*:\\s*([0-5]\\d)\\b");
    private static final Map<String, List<String>> NEARBY_REGIONS = Map.ofEntries(
        Map.entry("홍대", List.of("홍대", "홍대입구", "연남", "연남동", "합정", "합정동", "상수", "상수동", "망원", "망원동", "서교동", "동교동", "마포")),
        Map.entry("강남", List.of("강남", "강남역", "신논현", "역삼", "역삼동", "선릉", "삼성", "삼성동", "논현", "논현동", "신사", "신사동", "압구정")),
        Map.entry("성수", List.of("성수", "성수동", "뚝섬", "서울숲", "건대", "건대입구", "왕십리", "한양대")),
        Map.entry("잠실", List.of("잠실", "송파", "송파구", "석촌", "석촌호수", "방이", "강동", "올림픽공원")),
        Map.entry("건대", List.of("건대", "건대입구", "구의", "자양", "성수", "어린이대공원")),
        Map.entry("여의도", List.of("여의도", "영등포", "당산", "국회의사당", "샛강")),
        Map.entry("종로", List.of("종로", "광화문", "경복궁", "안국", "을지로", "시청", "중구")),
        Map.entry("이태원", List.of("이태원", "한남", "한남동", "용산", "해방촌", "녹사평")),
        Map.entry("서울", List.of(
            "서울", "강남", "서초", "송파", "강동", "마포", "서대문", "은평", "종로", "중구",
            "용산", "성동", "광진", "동대문", "중랑", "성북", "강북", "도봉", "노원", "양천",
            "강서", "구로", "금천", "영등포", "동작", "관악"
        ))
    );
    private static final Set<ReservationStatus> BLOCKING_RESERVATION_STATUSES = Set.of(
        ReservationStatus.PENDING,
        ReservationStatus.CONFIRMED
    );

    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiApiUrl;

    public AiPickResultDto recommend(String rawQuery, boolean expandNearby) {
        return recommend(rawQuery, expandNearby, Set.of());
    }

    public AiPickResultDto recommend(String rawQuery, boolean expandNearby, Set<Long> excludedItemIds) {
        AiPickQueryDto parsed = parseQueryWithGeminiOrRule(rawQuery);
        List<Item> candidates = loadCandidatesFromDb(parsed, expandNearby);
        candidates = filterUnavailableByRequestedDateTime(candidates, parsed);
        candidates = enrichCandidatesToTarget(candidates, parsed);
        candidates = filterUnavailableByRequestedDateTime(candidates, parsed);
        candidates = excludeItems(candidates, excludedItemIds);
        List<ScoredItem> ranked = rankCandidates(candidates, parsed);
        List<AiRecommendationDto> recommendations = mapRecommendations(ranked, parsed);
        boolean needsExpandConfirm = !expandNearby && recommendations.isEmpty() && parsed.location() != null;
        List<ChatMessageDto> messages = buildMessages(parsed, recommendations, needsExpandConfirm);
        return new AiPickResultDto(parsed, messages, recommendations, needsExpandConfirm, buildNearbyLabel(parsed.location()));
    }

    private List<Item> excludeItems(List<Item> candidates, Set<Long> excludedItemIds) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (excludedItemIds == null || excludedItemIds.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
            .filter(item -> item != null && item.getId() != null)
            .filter(item -> !excludedItemIds.contains(item.getId()))
            .toList();
    }

    private List<Item> filterUnavailableByRequestedDateTime(List<Item> candidates, AiPickQueryDto parsed) {
        if (candidates == null || candidates.isEmpty() || !hasText(parsed.requestedDate())) {
            return candidates == null ? List.of() : candidates;
        }
        LocalDate requestedDate;
        try {
            requestedDate = LocalDate.parse(parsed.requestedDate());
        } catch (Exception ignored) {
            return candidates;
        }

        List<Long> photographerIds = candidates.stream()
            .map(item -> item.getPhotographer() == null ? null : item.getPhotographer().getId())
            .filter(id -> id != null)
            .distinct()
            .toList();
        if (photographerIds.isEmpty()) {
            return candidates;
        }

        // DB 시간대(UTC 저장)와 사용자 시간대(Asia/Seoul) 차이를 흡수하기 위해
        // 조회 범위를 넉넉히 잡고, 이후 KST 기준 날짜/시간으로 한 번 더 필터링한다.
        LocalDateTime dayStart = requestedDate.minusDays(1).atStartOfDay();
        LocalDateTime dayEnd = requestedDate.plusDays(2).atStartOfDay();
        List<Reservation> dayReservations = reservationRepository.findByPhotographerIdsInRange(
            photographerIds, dayStart, dayEnd, BLOCKING_RESERVATION_STATUSES
        );
        if (dayReservations.isEmpty()) {
            return candidates;
        }
        List<Reservation> sameDayReservations = dayReservations.stream()
            .filter(reservation -> reservation.getScheduledAt().toLocalDate().equals(requestedDate))
            .toList();
        if (sameDayReservations.isEmpty()) {
            return candidates;
        }

        Integer requestedHour = parseRequestedHour(parsed.rawQuery());
        if (requestedHour == null) {
            // 시간 미입력 시에는 작가/상품을 통째로 제외하지 않는다.
            // (같은 날짜라도 다른 시간 슬롯이 비어 있을 수 있음)
            return candidates;
        }

        LocalDateTime requestedStart = requestedDate.atTime(LocalTime.of(requestedHour, 0));
        LocalDateTime requestedEnd = requestedStart.plusHours(1);
        Set<Long> blockedPhotographerIds = new HashSet<>();
        for (Reservation reservation : sameDayReservations) {
            LocalDateTime reservationStart = reservation.getScheduledAt();
            int durationMinutes = reservation.getDurationMinutes() == null ? 60 : reservation.getDurationMinutes();
            LocalDateTime reservationEnd = reservationStart.plusMinutes(durationMinutes);
            if (requestedStart.isBefore(reservationEnd) && requestedEnd.isAfter(reservationStart)) {
                blockedPhotographerIds.add(reservation.getPhotographer().getId());
            }
        }

        if (blockedPhotographerIds.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
            .filter(item -> item != null && item.getPhotographer() != null && item.getPhotographer().getId() != null)
            .filter(item -> !blockedPhotographerIds.contains(item.getPhotographer().getId()))
            .toList();
    }

    private Integer parseRequestedHour(String rawQuery) {
        if (!hasText(rawQuery)) {
            return null;
        }
        Matcher colonMatcher = HOUR_COLON_PATTERN.matcher(rawQuery);
        if (colonMatcher.find()) {
            int hour;
            try {
                hour = Integer.parseInt(colonMatcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
            if (hour < 0 || hour > 23) {
                return null;
            }
            return hour;
        }
        Matcher matcher = HOUR_PATTERN.matcher(rawQuery);
        if (!matcher.find()) {
            return null;
        }
        int hour;
        try {
            hour = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException ignored) {
            return null;
        }
        if (hour < 0 || hour > 24) {
            return null;
        }
        String meridiem = matcher.group(1);
        if ("오전".equals(meridiem)) {
            return hour == 12 ? 0 : hour;
        }
        if ("오후".equals(meridiem)) {
            return hour == 12 ? 12 : hour + 12;
        }
        if (hour == 24) {
            return 0;
        }
        return hour;
    }

    private AiPickQueryDto parseQueryWithGeminiOrRule(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.isBlank()) {
            return new AiPickQueryDto("", null, null, null, null, List.of(), null);
        }
        AiPickQueryDto ruleParsed = parseQueryByRule(query);
        AiPickQueryDto geminiParsed = parseQueryWithGemini(query);
        if (geminiParsed == null) {
            return sanitizeParsedQuery(normalizeRequestedDate(ruleParsed, query));
        }
        AiPickQueryDto merged = mergeParsedQuery(geminiParsed, ruleParsed);
        AiPickQueryDto categoryAligned = alignCategoryWithRule(merged, ruleParsed);
        AiPickQueryDto locationAligned = alignLocationWithRule(categoryAligned, ruleParsed);
        return sanitizeParsedQuery(normalizeRequestedDate(locationAligned, query));
    }

    /**
     * 카테고리는 원문 키워드(rule)를 우선한다.
     * (예: "커플사진" 입력인데 Gemini가 "가족"으로 해석하는 오분류 방지)
     */
    private AiPickQueryDto alignCategoryWithRule(AiPickQueryDto merged, AiPickQueryDto ruleParsed) {
        if (merged == null) return ruleParsed;
        if (ruleParsed == null || ruleParsed.category() == null || ruleParsed.category().isBlank()) {
            return new AiPickQueryDto(
                merged.rawQuery(),
                merged.requestedDate(),
                merged.location(),
                merged.maxPrice(),
                normalizeCategory(merged.category()),
                merged.styleTags(),
                merged.sort()
            );
        }
        String category = normalizeCategory(ruleParsed.category());
        return new AiPickQueryDto(
            merged.rawQuery(),
            merged.requestedDate(),
            merged.location(),
            merged.maxPrice(),
            category,
            merged.styleTags(),
            merged.sort()
        );
    }

    /**
     * 지역은 원문(rule) 값이 더 구체적일 때 우선한다.
     * (예: Gemini가 "강남"을 "서울"로 넓혀버리는 경우 방지)
     */
    private AiPickQueryDto alignLocationWithRule(AiPickQueryDto merged, AiPickQueryDto ruleParsed) {
        if (merged == null) return ruleParsed;
        if (ruleParsed == null || !hasText(ruleParsed.location())) {
            return merged;
        }

        String mergedLocation = merged.location();
        String ruleLocation = ruleParsed.location();
        String alignedLocation = mergedLocation;

        if (!hasText(mergedLocation) || isBroadLocation(mergedLocation)) {
            alignedLocation = ruleLocation;
        }

        return new AiPickQueryDto(
            merged.rawQuery(),
            merged.requestedDate(),
            alignedLocation,
            merged.maxPrice(),
            merged.category(),
            merged.styleTags(),
            merged.sort()
        );
    }

    private boolean isBroadLocation(String location) {
        if (!hasText(location)) {
            return false;
        }
        String normalized = location.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return normalized.equals("서울")
            || normalized.equals("서울시")
            || normalized.equals("서울전체")
            || normalized.equals("수도권")
            || normalized.equals("전국");
    }

    /**
     * 파싱 JSON 조건을 DB WHERE(태그/주소/가격)에 반영한 후보 풀.
     * 조건이 없는 검색이면 점수순으로만 제한, 지역+비확장 시 매칭 없으면 비움(상위 쿼리에서 expand 안내).
     */
    private List<Item> loadCandidatesFromDb(AiPickQueryDto parsed, boolean expandNearby) {
        String loc = hasText(parsed.location()) ? parsed.location() : null;
        List<String> catKws = parsed.category() != null ? categoryKeywords(parsed.category()) : List.of();
        boolean hasCat = !catKws.isEmpty();
        boolean hasLoc = loc != null;
        boolean hasPrice = parsed.maxPrice() != null;
        List<String> near = (loc != null) ? nearbyRegionKeywords(loc) : List.of();
        AiItemQuerySpec.AiItemSort sort = resolveItemSort(parsed.sort());
        int lim = AI_PICK_CANDIDATE_DB_LIMIT;

        if (!hasLoc) {
            List<Item> r;
            r = hasCat
                ? itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_STRICT, catKws,
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, lim))
                : null;
            if (r != null && !r.isEmpty()) {
                return r;
            }
            r = hasCat
                ? itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, lim))
                : null;
            if (r != null && !r.isEmpty()) {
                return r;
            }
            r = hasCat
                ? itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE_RELAX_130 : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, lim))
                : null;
            if (r != null && !r.isEmpty()) {
                return r;
            }
            r = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.ANY, List.of(),
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE_RELAX_130 : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, lim)
            );
            if (!r.isEmpty()) {
                return r;
            }
            // 마지막 안전망: 카테고리는 유지하지 못하더라도 0건을 피하기 위해 전체 후보를 반환
            return itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.ANY, List.of(),
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    AiItemQuerySpec.PriceMode.ANY, null,
                    sort, AI_PICK_GLOBAL_FALLBACK_LIMIT)
            );
        }

        // 지역이 있는 경우: 비확장이어도 별칭 정규화 키워드(행정동/생활권) 기준으로 직접 매칭한다.
        if (!expandNearby) {
            int rounds = hasPrice ? 3 : 1;
            for (int round = 0; round < rounds; round++) {
                AiItemQuerySpec.PriceMode priceForRound = pickPriceModeByRound(hasPrice, round);
                if (hasCat) {
                    List<Item> r = itemRepository.findForAiPick(
                        new AiItemQuerySpec(
                            AiItemQuerySpec.CategoryMode.TAG_STRICT, catKws,
                            AiItemQuerySpec.LocationMode.OR_KEYWORDS, null, near,
                            priceForRound, parsed.maxPrice(),
                            sort, lim)
                    );
                    if (!r.isEmpty()) {
                        return r;
                    }
                    r = itemRepository.findForAiPick(
                        new AiItemQuerySpec(
                            AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                            AiItemQuerySpec.LocationMode.OR_KEYWORDS, null, near,
                            priceForRound, parsed.maxPrice(),
                            sort, lim)
                    );
                    if (!r.isEmpty()) {
                        return r;
                    }
                } else {
                    List<Item> r = itemRepository.findForAiPick(
                        new AiItemQuerySpec(
                            AiItemQuerySpec.CategoryMode.ANY, List.of(),
                            AiItemQuerySpec.LocationMode.OR_KEYWORDS, null, near,
                            priceForRound, parsed.maxPrice(),
                            sort, lim)
                    );
                    if (!r.isEmpty()) {
                        return r;
                    }
                }
            }
            return List.of();
        }

        // 지역+인접 확장 허용: 단일 → OR 키워드 순
        int rounds = hasPrice ? 3 : 1;
        for (int round = 0; round < rounds; round++) {
            AiItemQuerySpec.PriceMode priceForRound = pickPriceModeByRound(hasPrice, round);
            if (hasCat) {
                List<Item> r = itemRepository.findForAiPick(
                    new AiItemQuerySpec(
                        AiItemQuerySpec.CategoryMode.TAG_STRICT, catKws,
                        AiItemQuerySpec.LocationMode.CONTAINS, loc, null,
                        priceForRound, parsed.maxPrice(),
                        sort, lim)
                );
                if (!r.isEmpty()) {
                    return r;
                }
                r = itemRepository.findForAiPick(
                    new AiItemQuerySpec(
                        AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                        AiItemQuerySpec.LocationMode.CONTAINS, loc, null,
                        priceForRound, parsed.maxPrice(),
                        sort, lim)
                );
                if (!r.isEmpty()) {
                    return r;
                }
            } else {
                List<Item> r = itemRepository.findForAiPick(
                    new AiItemQuerySpec(
                        AiItemQuerySpec.CategoryMode.ANY, List.of(),
                        AiItemQuerySpec.LocationMode.CONTAINS, loc, null,
                        priceForRound, parsed.maxPrice(),
                        sort, lim)
                );
                if (!r.isEmpty()) {
                    return r;
                }
            }
        }
        for (int round = 0; round < rounds; round++) {
            AiItemQuerySpec.PriceMode priceForRound = pickPriceModeByRound(hasPrice, round);
            if (hasCat) {
                List<Item> r = itemRepository.findForAiPick(
                    new AiItemQuerySpec(
                        AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                        AiItemQuerySpec.LocationMode.OR_KEYWORDS, null, near,
                        priceForRound, parsed.maxPrice(),
                        sort, lim)
                );
                if (!r.isEmpty()) {
                    return r;
                }
            } else {
                List<Item> r = itemRepository.findForAiPick(
                    new AiItemQuerySpec(
                        AiItemQuerySpec.CategoryMode.ANY, List.of(),
                        AiItemQuerySpec.LocationMode.OR_KEYWORDS, null, near,
                        priceForRound, parsed.maxPrice(),
                        sort, lim)
                );
                if (!r.isEmpty()) {
                    return r;
                }
            }
        }

        // 확장 동의까지 받은 뒤에는 0건으로 끝나지 않도록 최종 폴백을 둔다.
        if (hasCat) {
            List<Item> categoryWide = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE_RELAX_130 : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            if (!categoryWide.isEmpty()) {
                return categoryWide;
            }
            List<Item> categoryWideNoBudget = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_OR_TEXT, catKws,
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    AiItemQuerySpec.PriceMode.ANY, null,
                    sort, AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            if (!categoryWideNoBudget.isEmpty()) {
                return categoryWideNoBudget;
            }
        } else {
            List<Item> cityWide = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.ANY, List.of(),
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    hasPrice ? AiItemQuerySpec.PriceMode.LTE_RELAX_130 : AiItemQuerySpec.PriceMode.ANY, parsed.maxPrice(),
                    sort, AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            return cityWide.isEmpty() ? itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.ANY, List.of(),
                    AiItemQuerySpec.LocationMode.ANY, null, null,
                    AiItemQuerySpec.PriceMode.ANY, null,
                    sort, AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            ) : cityWide;
        }

        return List.of();
    }

    /**
     * 예산 필터 단계:
     * 1) 요청 예산 이내
     * 2) 요청 예산의 130% 이내
     * 3) 예산 무시(비슷한 범위 추천 허용)
     */
    private AiItemQuerySpec.PriceMode pickPriceModeByRound(boolean hasPrice, int round) {
        if (!hasPrice) {
            return AiItemQuerySpec.PriceMode.ANY;
        }
        if (round == 0) {
            return AiItemQuerySpec.PriceMode.LTE;
        }
        if (round == 1) {
            return AiItemQuerySpec.PriceMode.LTE_RELAX_130;
        }
        return AiItemQuerySpec.PriceMode.ANY;
    }

    private List<ScoredItem> rankCandidates(List<Item> candidates, AiPickQueryDto parsed) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<ScoredItem> scored = candidates.stream()
            .map(item -> new ScoredItem(item, calculateMatchScore(item, parsed), ""))
            .sorted(Comparator
                .comparingInt((ScoredItem si) -> locationPriority(si.item(), parsed)).reversed()
                .thenComparing(Comparator.comparingInt(ScoredItem::score).reversed())
                .thenComparing(Comparator.comparingDouble((ScoredItem si) ->
                    si.item().getReviewCount() == 0 ? 0.0 :
                        (double) si.item().getReviewScore() / si.item().getReviewCount()).reversed())
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

    /**
     * 1~2개만 남았을 때만 완화 후보를 추가해 최대 3개에 가깝게 채운다.
     * 0개 케이스는 기존 확장 제안 UX를 위해 유지한다.
     */
    private List<Item> enrichCandidatesToTarget(List<Item> baseCandidates, AiPickQueryDto parsed) {
        if (baseCandidates == null || baseCandidates.isEmpty() || baseCandidates.size() >= RECOMMENDATION_COUNT) {
            return baseCandidates == null ? List.of() : baseCandidates;
        }

        List<Item> merged = new ArrayList<>(baseCandidates);
        Set<Long> seenIds = new HashSet<>();
        for (Item item : merged) {
            if (item != null && item.getId() != null) {
                seenIds.add(item.getId());
            }
        }

        AiItemQuerySpec.AiItemSort sort = resolveItemSort(parsed.sort());
        List<String> catKws = parsed.category() != null ? categoryKeywords(parsed.category()) : List.of();
        boolean hasCat = !catKws.isEmpty();
        boolean hasLocation = hasText(parsed.location());
        List<String> nearKeywords = hasLocation ? nearbyRegionKeywords(parsed.location()) : List.of();
        AiItemQuerySpec.PriceMode relaxedPrice = parsed.maxPrice() == null
            ? AiItemQuerySpec.PriceMode.ANY
            : AiItemQuerySpec.PriceMode.LTE_RELAX_130;

        // 보충 단계에서는 "카테고리 고정, 지역만 확장" 원칙 유지
        List<Item> broad = itemRepository.findForAiPick(
            new AiItemQuerySpec(
                hasCat ? AiItemQuerySpec.CategoryMode.TAG_STRICT : AiItemQuerySpec.CategoryMode.ANY,
                hasCat ? catKws : List.of(),
                hasLocation ? AiItemQuerySpec.LocationMode.OR_KEYWORDS : AiItemQuerySpec.LocationMode.ANY,
                null,
                hasLocation ? nearKeywords : null,
                relaxedPrice,
                parsed.maxPrice(),
                sort,
                AI_PICK_GLOBAL_FALLBACK_LIMIT
            )
        );
        appendMissingUpToLimit(merged, broad, seenIds, RECOMMENDATION_COUNT);

        // 지역 조건이 있는 경우 3순위 보충은 "유사 카테고리"가 아니라 "지역 범위 확장"으로 처리
        if (hasCat && hasLocation && merged.size() < RECOMMENDATION_COUNT) {
            List<Item> widerRegion = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_STRICT,
                    catKws,
                    AiItemQuerySpec.LocationMode.ANY,
                    null,
                    null,
                    relaxedPrice,
                    parsed.maxPrice(),
                    sort,
                    AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            appendMissingUpToLimit(merged, widerRegion, seenIds, RECOMMENDATION_COUNT);
        }

        if (hasCat && hasLocation && merged.size() < RECOMMENDATION_COUNT && parsed.maxPrice() != null) {
            List<Item> widerRegionNoBudget = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.TAG_STRICT,
                    catKws,
                    AiItemQuerySpec.LocationMode.ANY,
                    null,
                    null,
                    AiItemQuerySpec.PriceMode.ANY,
                    null,
                    sort,
                    AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            appendMissingUpToLimit(merged, widerRegionNoBudget, seenIds, RECOMMENDATION_COUNT);
        }

        // 카테고리 조건이 있을 때는 ANY 글로벌 보충을 하지 않는다(카테고리 오염 방지).
        if (!hasCat && merged.size() < RECOMMENDATION_COUNT) {
            List<Item> global = itemRepository.findForAiPick(
                new AiItemQuerySpec(
                    AiItemQuerySpec.CategoryMode.ANY,
                    List.of(),
                    hasLocation ? AiItemQuerySpec.LocationMode.OR_KEYWORDS : AiItemQuerySpec.LocationMode.ANY,
                    null,
                    hasLocation ? nearKeywords : null,
                    AiItemQuerySpec.PriceMode.ANY,
                    null,
                    sort,
                    AI_PICK_GLOBAL_FALLBACK_LIMIT
                )
            );
            appendMissingUpToLimit(merged, global, seenIds, RECOMMENDATION_COUNT);
        }

        return merged;
    }

    private void appendMissingUpToLimit(List<Item> target, List<Item> source, Set<Long> seenIds, int limit) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (Item item : source) {
            if (target.size() >= limit || item == null || item.getId() == null) {
                break;
            }
            if (seenIds.add(item.getId())) {
                target.add(item);
            }
        }
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
                (double) item.getReviewScore() / item.getReviewCount(),
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
        String priceText = parsed.maxPrice() == null ? "예산 조건 미지정" : String.format("%,d원", parsed.maxPrice());
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
            messages.add(new ChatMessageDto("ai", "요청하신 지역에서 바로 매칭되는 결과는 없어요.<br>범위를 조금 넓혀서 추천해드릴까요?"));
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
        String sort = parseSortFromText(query);

        return new AiPickQueryDto(query, date, location, maxPrice, category, styleTags, sort);
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
              "style_tags": string[],
              "sort": string | null
            }

            규칙:
            - max_price는 숫자(원 단위 정수)만 반환
            - date는 가능하면 YYYY-MM-DD 형식 문자열로 반환
            - 추출 불가한 값은 null
            - style_tags는 없으면 빈 배열
            - sort는 "score" 또는 "random" 만 사용. 사용자가 랜덤/무작위/다양하게/추첨 등을 원하면 "random", 평점·인기순을 원하면 "score", 판단 불가면 null

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
        String sort = normalizeSortKey(asNullableText(root.path("sort")));
        return new AiPickQueryDto(rawQuery, date, location, maxPrice, category, styleTags, sort);
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

        String monthDay = parseMonthDayToCurrentYear(query);
        if (monthDay != null) {
            return monthDay;
        }

        return resolveRelativeDate(query);
    }

    private AiPickQueryDto normalizeRequestedDate(AiPickQueryDto parsed, String rawQuery) {
        if (parsed == null) return null;
        String relative = resolveRelativeDate(rawQuery);
        if (relative == null) {
            return parsed;
        }
        return new AiPickQueryDto(
            parsed.rawQuery(),
            relative,
            parsed.location(),
            parsed.maxPrice(),
            parsed.category(),
            parsed.styleTags(),
            parsed.sort()
        );
    }

    /**
     * 상대 날짜 표현을 현재 시점(LocalDate.now()) 기준 실제 날짜로 변환.
     * 반환 형식: YYYY-MM-DD
     */
    private String resolveRelativeDate(String query) {
        String q = query == null ? "" : query.replaceAll("\\s+", "");
        if (q.isBlank()) return null;

        LocalDate now = LocalDate.now();

        if (q.contains("오늘")) return now.toString();
        if (q.contains("내일")) return now.plusDays(1).toString();
        if (q.contains("모레")) return now.plusDays(2).toString();

        if (q.contains("다음주말")) return now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString();
        if (q.contains("이번주말")) return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toString();
        if (q.contains("주말")) return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toString();
        if (q.contains("평일")) return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).toString();

        if (q.contains("다다음주")) {
            for (DayOfWeek day : List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )) {
                if (containsKorWeekday(q, day)) {
                    LocalDate nextWeekMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    LocalDate nextNextWeekMonday = nextWeekMonday.plusWeeks(1);
                    return nextNextWeekMonday.with(TemporalAdjusters.nextOrSame(day)).toString();
                }
            }
        }

        if (q.contains("다음주")) {
            for (DayOfWeek day : List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )) {
                if (containsKorWeekday(q, day)) {
                    LocalDate nextWeekMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    return nextWeekMonday.with(TemporalAdjusters.nextOrSame(day)).toString();
                }
            }
            return now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toString();
        }

        if (q.contains("이번주")) {
            for (DayOfWeek day : List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )) {
                if (containsKorWeekday(q, day)) {
                    return now.with(TemporalAdjusters.nextOrSame(day)).toString();
                }
            }
            return now.toString();
        }

        for (DayOfWeek day : List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )) {
            if (containsKorWeekday(q, day)) {
                return now.with(TemporalAdjusters.nextOrSame(day)).toString();
            }
        }

        return null;
    }

    private boolean containsKorWeekday(String q, DayOfWeek day) {
        return switch (day) {
            case MONDAY -> containsWeekdayAlias(q, "월");
            case TUESDAY -> containsWeekdayAlias(q, "화");
            case WEDNESDAY -> containsWeekdayAlias(q, "수");
            case THURSDAY -> containsWeekdayAlias(q, "목");
            case FRIDAY -> containsWeekdayAlias(q, "금");
            case SATURDAY -> containsWeekdayAlias(q, "토");
            case SUNDAY -> containsWeekdayAlias(q, "일");
        };
    }

    private boolean containsWeekdayAlias(String q, String shortDay) {
        if (!hasText(q) || !hasText(shortDay)) {
            return false;
        }
        if (q.contains(shortDay + "요일") || q.contains(shortDay + "욜")) {
            return true;
        }
        // 단일 요일 축약어는 오탐을 줄이기 위해 명확한 문맥(주 단위 표현/단독 입력)에서만 허용
        return q.contains("다음주" + shortDay)
            || q.contains("이번주" + shortDay)
            || q.contains("다다음주" + shortDay)
            || q.equals(shortDay);
    }

    private String parseMonthDayToCurrentYear(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        Matcher ko = MONTH_DAY_KO_PATTERN.matcher(query);
        if (ko.find()) {
            return toCurrentYearDate(ko.group(1), ko.group(2));
        }
        Matcher slash = MONTH_DAY_SLASH_PATTERN.matcher(query);
        if (slash.find()) {
            return toCurrentYearDate(slash.group(1), slash.group(2));
        }
        Matcher dash = MONTH_DAY_DASH_PATTERN.matcher(query);
        if (dash.find()) {
            return toCurrentYearDate(dash.group(1), dash.group(2));
        }
        return null;
    }

    private String toCurrentYearDate(String monthText, String dayText) {
        try {
            int month = Integer.parseInt(monthText);
            int day = Integer.parseInt(dayText);
            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            return date.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String parseLocation(String query) {
        String bestRegion = null;
        int bestKeywordLength = -1;
        for (Map.Entry<String, List<String>> entry : NEARBY_REGIONS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (query.contains(keyword)) {
                    if (keyword.length() > bestKeywordLength) {
                        bestKeywordLength = keyword.length();
                        bestRegion = entry.getKey();
                    }
                }
            }
        }
        if (bestRegion != null) {
            return bestRegion;
        }

        String[] preferredAliases = {
            "홍대", "강남", "성수", "잠실", "건대", "여의도", "종로", "이태원",
            "서울", "부산", "제주"
        };
        for (String alias : preferredAliases) {
            if (query.contains(alias)) {
                return alias;
            }
        }
        return null;
    }

    private String parseCategory(String query) {
        return normalizeCategory(query);
    }

    private String normalizeCategory(String raw) {
        if (!hasText(raw)) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        if (normalized.contains("데이트") || normalized.contains("커플") || normalized.contains("연인")) return "데이트";
        if (normalized.contains("웨딩") || normalized.contains("브라이덜") || normalized.contains("본식")) return "웨딩";
        if (normalized.contains("가족") || normalized.contains("패밀리") || normalized.contains("아이") || normalized.contains("키즈")) return "가족";
        if (normalized.contains("졸업") || normalized.contains("학사") || normalized.contains("캠퍼스")) return "졸업";
        if (normalized.contains("프로필")
            || normalized.contains("취업")
            || normalized.contains("증명")
            || normalized.contains("이력서")
            || normalized.contains("면접")
            || normalized.contains("비즈니스")) return "프로필";
        if (normalized.contains("제품") || normalized.contains("상품") || normalized.contains("브랜딩")) return "제품";
        return null;
    }

    private List<String> parseStyleTags(String query) {
        List<String> styles = new ArrayList<>();
        if (query.contains("감성")) styles.add("감성");
        if (query.contains("자연")) styles.add("자연스러운");
        if (query.contains("깔끔")) styles.add("깔끔한");
        if (query.contains("빈티지")) styles.add("빈티지");
        if (query.contains("자유")) styles.add("자유로운");
        if (query.contains("시네마")) styles.add("시네마틱");
        if (query.contains("필름")) styles.add("필름");
        if (query.contains("따뜻")) styles.add("따뜻한");
        if (query.contains("모던")) styles.add("모던");
        if (query.contains("세련")) styles.add("세련된");
        if (query.contains("힙")) styles.add("힙한");
        if (query.contains("미니멀")) styles.add("미니멀");
        if (query.contains("화사")) styles.add("화사한");
        if (query.contains("다크")) styles.add("다크");
        if (query.contains("러블리")) styles.add("러블리");
        if (query.contains("로맨틱")) styles.add("로맨틱");
        if (query.contains("드라마틱")) styles.add("드라마틱");
        return styles;
    }

    private String buildReason(AiPickQueryDto parsed, Item item, int rank) {
        boolean locationMatched = parsed.location() != null && hasText(item.getAddress()) && item.getAddress().contains(parsed.location());
        boolean budgetMatched = parsed.maxPrice() != null && item.getDefaultPrice() <= parsed.maxPrice();
        boolean budgetNear = parsed.maxPrice() != null && !budgetMatched && item.getDefaultPrice() <= parsed.maxPrice() * 1.3;
        String region = extractRegion(item.getAddress(), parsed.location());
        String stylePhrase = pickStylePhrase(parsed.styleTags());
        String titleCue = pickTitleCue(item);
        String placePart = locationMatched ? region + " 쪽 촬영이 편하고" : "요청 분위기랑 잘 맞고";
        String stylePart = stylePhrase != null ? stylePhrase + " 무드가 잘 살아" : "전체 톤이 자연스럽고";
        String budgetPart = budgetMatched ? "예산에도 맞아서" : (budgetNear ? "예산은 조금 넘지만 구성이 좋아서" : "가격 대비 구성이 괜찮아서");
        String rankTone = rank == 1 ? "지금 조건에서 가장 먼저 추천드려요" : "지금 조건에서 같이 비교해보기 좋아요";

        // 추천 근거는 상품 톤을 살린 캐주얼한 한 문장으로 고정
        return titleCue + " " + placePart + " " + stylePart + " " + budgetPart + " " + rankTone + ".";
    }

    private String pickStylePhrase(List<String> styleTags) {
        if (styleTags == null || styleTags.isEmpty()) return null;
        String first = styleTags.get(0);
        if ("감성".equals(first)) return "감성적인";
        if ("자연스러운".equals(first)) return "자연스러운";
        if ("깔끔한".equals(first)) return "깔끔한";
        if ("빈티지".equals(first)) return "빈티지한";
        if ("자유로운".equals(first)) return "자유로운";
        if ("시네마틱".equals(first)) return "시네마틱한";
        if ("필름".equals(first)) return "필름톤";
        if ("따뜻한".equals(first)) return "따뜻한";
        if ("모던".equals(first)) return "모던한";
        if ("세련된".equals(first)) return "세련된";
        if ("힙한".equals(first)) return "힙한";
        if ("미니멀".equals(first)) return "미니멀한";
        if ("화사한".equals(first)) return "화사한";
        if ("다크".equals(first)) return "다크한";
        if ("러블리".equals(first)) return "러블리한";
        if ("로맨틱".equals(first)) return "로맨틱한";
        if ("드라마틱".equals(first)) return "드라마틱한";
        return first;
    }

    private String pickTitleCue(Item item) {
        String title = item == null ? null : item.getName();
        String desc = item == null ? null : item.getDescription();
        String source = (title == null ? "" : title) + " " + (desc == null ? "" : desc);

        if (containsAny(source, "자연광", "야외", "공원", "정원")) return "야외 컷이 강점이라";
        if (containsAny(source, "스튜디오", "실내", "조명")) return "실내 연출이 안정적이라";
        if (containsAny(source, "감성", "필름", "빈티지")) return "감성 톤이 확실해서";
        if (containsAny(source, "로맨틱", "데이트", "커플", "연인")) return "커플 분위기를 살리기 좋아서";
        if (containsAny(source, "깔끔", "모던", "미니멀")) return "깔끔한 결과물을 원할 때 잘 맞아서";

        if (title != null && !title.isBlank()) {
            String t = title.trim();
            if (t.length() > 18) {
                t = t.substring(0, 18).trim() + "...";
            }
            return "'" + t + "' 컨셉이 또렷해서";
        }
        return "전체 완성도가 좋아서";
    }

    private boolean containsAny(String source, String... keywords) {
        if (!hasText(source) || keywords == null) return false;
        String s = source.toLowerCase(Locale.ROOT);
        for (String k : keywords) {
            if (k != null && s.contains(k.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
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
        score += Math.min((int) ((double) item.getReviewScore() / item.getReviewCount() * 100) / 20, 10);
        return score;
    }

    /**
     * 지역 우선순위:
     * 2 = 요청 지역 직접 매칭, 1 = 인접 지역 매칭, 0 = 그 외
     */
    private int locationPriority(Item item, AiPickQueryDto parsed) {
        if (item == null || parsed == null || !hasText(parsed.location()) || !hasText(item.getAddress())) {
            return 0;
        }
        String address = item.getAddress();
        String location = parsed.location();
        if (address.contains(location)) {
            return 2;
        }
        List<String> near = nearbyRegionKeywords(location);
        for (String keyword : near) {
            if (hasText(keyword) && address.contains(keyword)) {
                return 1;
            }
        }
        return 0;
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
        if (value.contains("웨딩")) return List.of("웨딩", "본식", "스드메", "예식", "브라이덜");
        // "스냅"은 범용 단어라 데이트 카테고리에서 과매칭(졸업/제품/가족 스냅)을 유발함.
        if (value.contains("데이트") || value.contains("커플")) return List.of("데이트", "커플", "연인");
        if (value.contains("가족")) return List.of("가족", "패밀리", "베이비", "아이", "키즈", "돌");
        if (value.contains("졸업")) return List.of("졸업", "학사", "캠퍼스", "졸업사진");
        if (value.contains("프로필")) return List.of("프로필", "증명", "취업", "이력서");
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
        String sort = primary.sort() != null ? primary.sort() : fallback.sort();
        return new AiPickQueryDto(primary.rawQuery(), requestedDate, location, maxPrice, category, styleTags, sort);
    }

    /**
     * "실내/야외/스튜디오" 같은 촬영 환경 키워드는 지역 필터에서 제외한다.
     * (주소 contains 조건으로 들어가면 결과 0건이 되는 문제 방지)
     */
    private AiPickQueryDto sanitizeParsedQuery(AiPickQueryDto parsed) {
        String sanitizedLocation = isLocationLikeEnvironment(parsed.location()) ? null : parsed.location();
        return new AiPickQueryDto(
            parsed.rawQuery(),
            parsed.requestedDate(),
            sanitizedLocation,
            parsed.maxPrice(),
            parsed.category(),
            parsed.styleTags(),
            parsed.sort()
        );
    }

    private boolean isLocationLikeEnvironment(String location) {
        if (!hasText(location)) {
            return false;
        }
        String v = location.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return v.contains("실내")
            || v.contains("야외")
            || v.contains("스튜디오")
            || v.contains("스튜")
            || v.contains("정원")
            || v.contains("배경")
            || v.contains("자연광")
            || v.contains("역광")
            || v.contains("감성")
            || v.contains("빈티지")
            || v.contains("화이트톤")
            || v.contains("블랙톤");
    }

    /** null 또는 알 수 없는 값 → 평균 점수 순, "random" → DB RAND() 정렬. */
    private static AiItemQuerySpec.AiItemSort resolveItemSort(String sort) {
        if (sort == null) {
            return AiItemQuerySpec.AiItemSort.AVG_SCORE_DESC;
        }
        String s = sort.trim().toLowerCase(Locale.ROOT);
        if ("random".equals(s) || "rand".equals(s)) {
            return AiItemQuerySpec.AiItemSort.RANDOM;
        }
        return AiItemQuerySpec.AiItemSort.AVG_SCORE_DESC;
    }

    /** Gemini/텍스트의 sort 값을 "score" | "random" | null 로 통일. */
    private String normalizeSortKey(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        if (s.isEmpty() || "null".equals(s)) {
            return null;
        }
        if (s.contains("random") || "rand".equals(s) || "r".equals(s) || s.contains("shuffle")
            || s.contains("랜덤") || s.contains("무작위") || s.contains("다양") || s.contains("추첨")) {
            return "random";
        }
        if (s.contains("score") || s.equals("avg") || s.equals("rating") || s.contains("평점")
            || s.contains("인기") || s.contains("후기") || s.contains("best")) {
            return "score";
        }
        return null;
    }

    private String parseSortFromText(String query) {
        if (query == null) {
            return null;
        }
        String n = query.toLowerCase(Locale.ROOT);
        if (n.contains("랜덤") || n.contains("무작위") || n.contains("다양하게") || n.contains("추첨")
            || n.contains("섞어") || n.contains("랜덤으로")) {
            return "random";
        }
        return null;
    }

    private record ScoredItem(Item item, int score, String reason) {}
}
