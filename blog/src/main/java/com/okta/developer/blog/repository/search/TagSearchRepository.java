package com.okta.developer.blog.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.okta.developer.blog.domain.Tag;
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
 * Spring Data Elasticsearch repository for the {@link Tag} entity.
 */
public interface TagSearchRepository extends ReactiveElasticsearchRepository<Tag, String>, TagSearchRepositoryInternal {}

interface TagSearchRepositoryInternal {
    Flux<Tag> search(String query, Pageable pageable);
}

class TagSearchRepositoryInternalImpl implements TagSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    TagSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Tag> search(String query, Pageable pageable) {
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
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Tag.class).map(SearchHit::getContent);
    }
}
