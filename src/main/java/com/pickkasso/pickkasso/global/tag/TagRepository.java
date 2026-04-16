package com.pickkasso.pickkasso.global.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("select new com.pickkasso.pickkasso.global.tag.TagReference(t.id, t.name) from Tag t")
    List<TagReference> findAllTagReference();

    @Query("select new com.pickkasso.pickkasso.global.tag.TagReference(t.id, t.name) from Tag t where t.name = :name")
    Optional<TagReference> findTagByName(@Param("name") String name);
}
