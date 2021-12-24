package com.okta.developer.blog.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.okta.developer.blog.domain.Tag;
import com.okta.developer.blog.repository.TagRepository;
import com.okta.developer.blog.repository.search.TagSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Tag}.
 */
@Service
public class TagService {

    private final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    private final TagSearchRepository tagSearchRepository;

    public TagService(TagRepository tagRepository, TagSearchRepository tagSearchRepository) {
        this.tagRepository = tagRepository;
        this.tagSearchRepository = tagSearchRepository;
    }

    /**
     * Save a tag.
     *
     * @param tag the entity to save.
     * @return the persisted entity.
     */
    public Mono<Tag> save(Tag tag) {
        log.debug("Request to save Tag : {}", tag);
        return tagRepository.save(tag).flatMap(tagSearchRepository::save);
    }

    /**
     * Partially update a tag.
     *
     * @param tag the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Tag> partialUpdate(Tag tag) {
        log.debug("Request to partially update Tag : {}", tag);

        return tagRepository
            .findById(tag.getId())
            .map(existingTag -> {
                if (tag.getName() != null) {
                    existingTag.setName(tag.getName());
                }

                return existingTag;
            })
            .flatMap(tagRepository::save)
            .flatMap(savedTag -> {
                tagSearchRepository.save(savedTag);

                return Mono.just(savedTag);
            });
    }

    /**
     * Get all the tags.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<Tag> findAll(Pageable pageable) {
        log.debug("Request to get all Tags");
        return tagRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of tags available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return tagRepository.count();
    }

    /**
     * Returns the number of tags available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return tagSearchRepository.count();
    }

    /**
     * Get one tag by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<Tag> findOne(String id) {
        log.debug("Request to get Tag : {}", id);
        return tagRepository.findById(id);
    }

    /**
     * Delete the tag by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Tag : {}", id);
        return tagRepository.deleteById(id).then(tagSearchRepository.deleteById(id));
    }

    /**
     * Search for the tag corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<Tag> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Tags for query {}", query);
        return tagSearchRepository.search(query, pageable);
    }
}
