package com.pickkasso.pickkasso.global.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("select new com.pickkasso.pickkasso.global.tag.TagReference(t.id, t.name) from Tag t")
    List<TagReference> findAllTagReference();
}
