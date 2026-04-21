package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "WHERE t.name = :tagName " +
        "ORDER BY i.avgScore DESC ")
    List<Item> findScoreItemListByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "ORDER BY i.avgScore DESC ")
    List<Item> findScoreItemList(Pageable pageable);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "ORDER BY RAND() ")
    List<Item> findRandomItemList(Pageable pageable);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "WHERE i.photographer.id = :photographerId " +
        "ORDER BY i.createdAt DESC ")
    List<Item> findByPhotographerId(@Param("photographerId") Long photographerId);

    @Query("SELECT DISTINCT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "LEFT JOIN FETCH i.planList " +
        "WHERE i.id = :itemId")
    java.util.Optional<Item> findByIdWithDetails(@Param("itemId") Long itemId);
}
