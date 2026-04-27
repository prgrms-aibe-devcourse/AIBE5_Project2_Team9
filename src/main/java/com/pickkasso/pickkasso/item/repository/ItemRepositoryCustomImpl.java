package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.global.region.QRegionDto;
import com.pickkasso.pickkasso.global.tag.QTagReference;
import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.ItemType;
import com.pickkasso.pickkasso.item.entity.QItem;
import com.pickkasso.pickkasso.user.dto.photographer.QPhotographerSimpleCardDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {
    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private BooleanExpression tagEq(Tag tag) {
        return (tag != null) ? QItem.item.tag.eq(tag) : null;
    }

    private BooleanExpression itemTypeEq(ItemType itemType) {
        return (itemType != null) ? QItem.item.itemType.eq(itemType) : null;
    }

    private NumberExpression<Double> distanceExpression(Double lat, Double lng) {
        return Expressions.numberTemplate(Double.class,
            "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
            lat, QItem.item.lat, QItem.item.lng, lng
        );
    }

    // TODO: "공간 인덱스로 대상 row를 줄이고, 줄어든 row에만 ST_Distance_Sphere를 적용"이 더 효율적이라고 함
    private BooleanExpression withinDistance(Double lat, Double lng, Integer distance) {
        if (lat == null || lng == null || distance == null) return null;
        return distanceExpression(lat, lng).loe(distance);
    }
    private BooleanExpression withinDistance(NumberTemplate<Double> distanceExpr, Integer maxDistance) {
        if (distanceExpr == null || maxDistance == null) return null;
        return distanceExpr.loe(maxDistance * 1000);
    }

    private NumberTemplate<Double> buildDistanceExpression(Double lat, Double lng) {
        if (lat == null || lng == null) return null;
        return Expressions.numberTemplate(Double.class,
            "ST_Distance_Sphere(point({0}, {1}), point({2}, {3}))",
            QItem.item.lng, QItem.item.lat, lng, lat);
    }

    private OrderSpecifier<?> getOrderSpecifier(String orderBy, NumberTemplate<Double> distance) {
        if (orderBy == null) return QItem.item.reviewScore.divide(QItem.item.reviewCount).desc();

        return switch (orderBy) {
            case "score"            -> QItem.item.reviewScore.divide(QItem.item.reviewCount).desc();
            case "review"           -> QItem.item.reviewCount.desc();
            case "created-at-desc"  -> QItem.item.createdAt.desc();
            case "created-at-asc"   -> QItem.item.createdAt.asc();
            case "price-desc"       -> QItem.item.defaultPrice.desc();
            case "price-asc"        -> QItem.item.defaultPrice.asc();
            case "distance"         -> (distance == null) ? QItem.item.reviewScore.divide(QItem.item.reviewCount).desc() : distance.asc();
            default                 -> QItem.item.reviewScore.divide(QItem.item.reviewCount).desc();
        };
    }

    // 아직 날짜 관련 코드 구현 못함
    @Override
    public Page<ItemBoxDto> getSearchItemPage(ItemSearchCondition condition, int pageSize) {
        QItem item = QItem.item;

        Pageable pageable = PageRequest.of(
            (condition.getPage() != null) ? condition.getPage() - 1 : 0, pageSize
        );
        NumberTemplate<Double> distance = (condition.getLat() != null && condition.getLng() != null)
            ? Expressions.numberTemplate(Double.class,
            "ST_Distance_Sphere(point({0}, {1}), point({2}, {3}))",
            item.lng, item.lat, condition.getLng(), condition.getLat())
            : Expressions.numberTemplate(Double.class, "null");
        NumberTemplate<Double> distanceExpr = buildDistanceExpression(condition.getLat(), condition.getLng());

        List<ItemBoxDto> content = queryFactory
            .select(Projections.constructor(
                ItemBoxDto.class,
                item.id,
                item.name,
                item.thumbnailImgUrl,
                new QTagReference(item.tag.id, item.tag.name, item.tag.emoji),
                new QPhotographerSimpleCardDto(Expressions.nullExpression(String.class), item.photographer.name),
                new QRegionDto(item.address, item.detailAddress, item.lat, item.lng),
                item.defaultPrice,
                item.itemType,
                item.reviewScore,
                item.reviewCount,
                item.purchaseCount,
                // distance
                distanceExpr != null ? distanceExpr : Expressions.nullExpression(Double.class)
            )).from(item)
            .join(item.tag)
            .join(item.photographer)
            .where(
                tagEq(condition.getTag()),
                itemTypeEq(condition.getItemType()),
                // withinDistance(condition.getLat(), condition.getLng(), condition.getDistance())
                withinDistance(distanceExpr, condition.getDistance())
            )
            .orderBy(getOrderSpecifier(condition.getOrderBy(), distanceExpr))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        long total = queryFactory
            .select(item.count())
            .from(item)
            .join(item.tag)
            .where(
                tagEq(condition.getTag()),
                itemTypeEq(condition.getItemType()),
                withinDistance(condition.getLat(), condition.getLng(), condition.getDistance())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Item> findForAiPick(AiItemQuerySpec spec) {
        QItem item = QItem.item;
        BooleanBuilder where = new BooleanBuilder();

        switch (spec.categoryMode()) {
            case TAG_STRICT -> {
                com.querydsl.core.types.Predicate p = buildCategoryTagStrict(item, spec.categoryKeywords());
                if (p != null) {
                    where.and(p);
                }
            }
            case TAG_OR_TEXT -> {
                com.querydsl.core.types.Predicate p = buildCategoryTagOrText(item, spec.categoryKeywords());
                if (p != null) {
                    where.and(p);
                }
            }
            case ANY -> { }
        }

        if (spec.locationMode() == AiItemQuerySpec.LocationMode.CONTAINS) {
            if (spec.singleLocation() != null) {
                where.and(item.address.contains(spec.singleLocation()));
            }
        } else if (spec.locationMode() == AiItemQuerySpec.LocationMode.OR_KEYWORDS) {
            com.querydsl.core.types.Predicate p = buildAddressOrAny(item, spec.locationOrKeywords());
            if (p != null) {
                where.and(p);
            }
        }

        if (spec.priceMode() == AiItemQuerySpec.PriceMode.LTE) {
            if (spec.maxPrice() != null) {
                where.and(item.defaultPrice.loe(spec.maxPrice()));
            }
        } else if (spec.priceMode() == AiItemQuerySpec.PriceMode.LTE_RELAX_130) {
            if (spec.maxPrice() != null) {
                int p = (int) (spec.maxPrice() * 1.3);
                where.and(item.defaultPrice.loe(p));
            }
        }

        JPAQuery<Item> query = queryFactory
            .selectFrom(item)
            .innerJoin(item.tag).fetchJoin()
            .innerJoin(item.photographer).fetchJoin()
            .where(where)
            .limit(spec.limit() > 0 ? spec.limit() : 150);

        if (spec.sort() == AiItemQuerySpec.AiItemSort.RANDOM) {
            query.orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc());
        } else {
            query.orderBy(
                item.reviewScore.divide(item.reviewCount).desc(),
                item.reviewCount.desc()
            );
        }

        return query.fetch();
    }

    private com.querydsl.core.types.Predicate buildCategoryTagStrict(QItem item, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        BooleanBuilder b = new BooleanBuilder();
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) {
                continue;
            }
            String k = kw.toLowerCase(Locale.ROOT);
            b.or(item.tag.name.toLowerCase().contains(k));
        }
        if (!b.hasValue()) {
            return null;
        }
        return b.getValue();
    }

    private com.querydsl.core.types.Predicate buildCategoryTagOrText(QItem item, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        BooleanBuilder perKeyword = new BooleanBuilder();
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) {
                continue;
            }
            String k = kw.toLowerCase(Locale.ROOT);
            perKeyword.or(
                item.tag.name.toLowerCase().contains(k)
                    .or(item.name.toLowerCase().contains(k))
                    .or(item.description.contains(kw))
            );
        }
        if (!perKeyword.hasValue()) {
            return null;
        }
        return perKeyword.getValue();
    }

    private com.querydsl.core.types.Predicate buildAddressOrAny(QItem item, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        BooleanBuilder b = new BooleanBuilder();
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) {
                continue;
            }
            b.or(item.address.contains(kw));
        }
        if (!b.hasValue()) {
            return null;
        }
        return b.getValue();
    }
}

