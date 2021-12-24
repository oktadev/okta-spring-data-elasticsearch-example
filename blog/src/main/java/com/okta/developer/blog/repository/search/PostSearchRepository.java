package com.okta.developer.blog.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.okta.developer.blog.domain.Post;
import java.util.List;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Post} entity.
 */
public interface PostSearchRepository extends ReactiveElasticsearchRepository<Post, String>, PostSearchRepositoryInternal {}

interface PostSearchRepositoryInternal {
    Flux<Post> search(String query, Pageable pageable);
}

class PostSearchRepositoryInternalImpl implements PostSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    PostSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Post> search(String query, Pageable pageable) {
        List<FieldSortBuilder> builders = new SortToFieldSortBuilderConverter().convert(pageable.getSort());

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery(query))
            .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        builders
            .stream()
            .forEach(builder -> {
                queryBuilder.withSort(builder);
            });

        NativeSearchQuery nativeSearchQuery = queryBuilder.build();
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Post.class).map(SearchHit::getContent);
    }
}
