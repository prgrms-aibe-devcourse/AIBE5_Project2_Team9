package com.pickkasso.pickkasso.global.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagReference> findAllTagReference() {
        return tagRepository.findAllTagReference();
    }

    @Transactional(readOnly = true)
    public Optional<Tag> findByName(String name) { return tagRepository.findTagByName(name); }
}
