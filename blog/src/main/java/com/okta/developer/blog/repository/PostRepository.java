package com.okta.developer.blog.repository;

import com.okta.developer.blog.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data MongoDB reactive repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllBy(Pageable pageable);

    @Query("{}")
    Flux<Post> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    Flux<Post> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Mono<Post> findOneWithEagerRelationships(String id);
}
