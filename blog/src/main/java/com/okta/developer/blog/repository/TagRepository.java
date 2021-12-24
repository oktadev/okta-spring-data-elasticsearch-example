package com.okta.developer.blog.repository;

import com.okta.developer.blog.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends ReactiveMongoRepository<Tag, String> {
    Flux<Tag> findAllBy(Pageable pageable);
}
