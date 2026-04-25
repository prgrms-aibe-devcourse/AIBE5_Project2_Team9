package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    @EntityGraph(attributePaths = {"tag", "photographer", "photographer.photographerProfile"})
    @Query("SELECT i FROM Item i WHERE i.id = :itemId")
    Optional<Item> findDetailById(@Param("itemId") Long itemId);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "WHERE t.name = :tagName " +
        "ORDER BY (i.reviewScore / i.reviewCount) DESC ")
    List<Item> findScoreItemListByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT i FROM Item i " +
        "JOIN FETCH i.tag t " +
        "JOIN FETCH i.photographer p " +
        "ORDER BY (i.reviewScore / i.reviewCount) DESC ")
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
    Optional<Item> findByIdWithDetails(@Param("itemId") Long itemId);

    @Query("SELECT i.address FROM Item i " +
           "WHERE i.photographer.id = :photographerId")
    List<String> findAddressByPhotographerId(@Param("photographerId") Long photographerId);

    @Query("SELECT i FROM Item i " +
           "JOIN FETCH i.tag " +
           "LEFT JOIN FETCH i.planList " +
           "WHERE i.id = :itemId " +
           "    AND i.photographer.id = :photographerId")
    Optional<Item> findByIdAndPhotographerId(@Param("itemId") Long itemId, @Param("photographerId") Long photographerId);

    @Query("SELECT i FROM Item i " +
        "LEFT JOIN FETCH i.itemImgList " +
        "WHERE i.id = :itemId")
    Optional<Item> findByIdWithImgList(@Param("itemId") Long itemId);

    @Query("SELECT i FROM Item i " +
        "LEFT JOIN FETCH i.tag " +
        "WHERE i.photographer.id = :photographerId ")
    List<Item> findItemByPhotographerId(@Param("photographerId") Long photographerId);
}
