package com.okta.developer.blog.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.okta.developer.blog.domain.Post;
import com.okta.developer.blog.repository.PostRepository;
import com.okta.developer.blog.repository.search.PostSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Post}.
 */
@Service
public class PostService {

    private final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    private final PostSearchRepository postSearchRepository;

    public PostService(PostRepository postRepository, PostSearchRepository postSearchRepository) {
        this.postRepository = postRepository;
        this.postSearchRepository = postSearchRepository;
    }

    /**
     * Save a post.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    public Mono<Post> save(Post post) {
        log.debug("Request to save Post : {}", post);
        return postRepository.save(post).flatMap(postSearchRepository::save);
    }

    /**
     * Partially update a post.
     *
     * @param post the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Post> partialUpdate(Post post) {
        log.debug("Request to partially update Post : {}", post);

        return postRepository
            .findById(post.getId())
            .map(existingPost -> {
                if (post.getTitle() != null) {
                    existingPost.setTitle(post.getTitle());
                }
                if (post.getContent() != null) {
                    existingPost.setContent(post.getContent());
                }
                if (post.getDate() != null) {
                    existingPost.setDate(post.getDate());
                }

                return existingPost;
            })
            .flatMap(postRepository::save)
            .flatMap(savedPost -> {
                postSearchRepository.save(savedPost);

                return Mono.just(savedPost);
            });
    }

    /**
     * Get all the posts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<Post> findAll(Pageable pageable) {
        log.debug("Request to get all Posts");
        return postRepository.findAllBy(pageable);
    }

    /**
     * Get all the posts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<Post> findAllWithEagerRelationships(Pageable pageable) {
        return postRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Returns the number of posts available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return postRepository.count();
    }

    /**
     * Returns the number of posts available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return postSearchRepository.count();
    }

    /**
     * Get one post by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<Post> findOne(String id) {
        log.debug("Request to get Post : {}", id);
        return postRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the post by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Post : {}", id);
        return postRepository.deleteById(id).then(postSearchRepository.deleteById(id));
    }

    /**
     * Search for the post corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<Post> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Posts for query {}", query);
        return postSearchRepository.search(query, pageable);
    }
}
