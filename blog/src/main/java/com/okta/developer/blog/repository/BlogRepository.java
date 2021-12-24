package com.okta.developer.blog.repository;

import com.okta.developer.blog.domain.Blog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the Blog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlogRepository extends ReactiveMongoRepository<Blog, String> {}
