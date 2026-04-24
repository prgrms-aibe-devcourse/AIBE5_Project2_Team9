package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.global.region.QRegionDto;
import com.pickkasso.pickkasso.global.tag.QTagReference;
import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
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
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
        if (orderBy == null) return QItem.item.avgScore.desc();

        return switch (orderBy) {
            case "score"            -> QItem.item.avgScore.desc();
            case "review"           -> QItem.item.reviewCount.desc();
            case "created-at-desc"  -> QItem.item.createdAt.desc();
            case "created-at-asc"   -> QItem.item.createdAt.asc();
            case "price-desc"       -> QItem.item.defaultPrice.desc();
            case "price-asc"        -> QItem.item.defaultPrice.asc();
            case "distance"         -> (distance == null) ? QItem.item.avgScore.desc() : distance.asc();
            default                 -> QItem.item.avgScore.desc();
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
                item.avgScore,
                item.defaultPrice,
                item.itemType,
                item.reviewCount,
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
}
