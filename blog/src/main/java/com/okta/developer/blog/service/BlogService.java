package com.okta.developer.blog.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.okta.developer.blog.domain.Blog;
import com.okta.developer.blog.repository.BlogRepository;
import com.okta.developer.blog.repository.search.BlogSearchRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Blog}.
 */
@Service
public class BlogService {

    private final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogRepository blogRepository;

    private final BlogSearchRepository blogSearchRepository;

    public BlogService(BlogRepository blogRepository, BlogSearchRepository blogSearchRepository) {
        this.blogRepository = blogRepository;
        this.blogSearchRepository = blogSearchRepository;
    }

    /**
     * Save a blog.
     *
     * @param blog the entity to save.
     * @return the persisted entity.
     */
    public Mono<Blog> save(Blog blog) {
        log.debug("Request to save Blog : {}", blog);
        return blogRepository.save(blog).flatMap(blogSearchRepository::save);
    }

    /**
     * Partially update a blog.
     *
     * @param blog the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Blog> partialUpdate(Blog blog) {
        log.debug("Request to partially update Blog : {}", blog);

        return blogRepository
            .findById(blog.getId())
            .map(existingBlog -> {
                if (blog.getName() != null) {
                    existingBlog.setName(blog.getName());
                }
                if (blog.getHandle() != null) {
                    existingBlog.setHandle(blog.getHandle());
                }

                return existingBlog;
            })
            .flatMap(blogRepository::save)
            .flatMap(savedBlog -> {
                blogSearchRepository.save(savedBlog);

                return Mono.just(savedBlog);
            });
    }

    /**
     * Get all the blogs.
     *
     * @return the list of entities.
     */
    public Flux<Blog> findAll() {
        log.debug("Request to get all Blogs");
        return blogRepository.findAll();
    }

    /**
     * Returns the number of blogs available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return blogRepository.count();
    }

    /**
     * Returns the number of blogs available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return blogSearchRepository.count();
    }

    /**
     * Get one blog by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<Blog> findOne(String id) {
        log.debug("Request to get Blog : {}", id);
        return blogRepository.findById(id);
    }

    /**
     * Delete the blog by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Blog : {}", id);
        return blogRepository.deleteById(id).then(blogSearchRepository.deleteById(id));
    }

    /**
     * Search for the blog corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    public Flux<Blog> search(String query) {
        log.debug("Request to search Blogs for query {}", query);
        return blogSearchRepository.search(query);
    }
}
