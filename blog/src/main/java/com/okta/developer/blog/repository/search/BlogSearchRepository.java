package com.okta.developer.blog.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.okta.developer.blog.domain.Blog;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Blog} entity.
 */
public interface BlogSearchRepository extends ReactiveElasticsearchRepository<Blog, String>, BlogSearchRepositoryInternal {}

interface BlogSearchRepositoryInternal {
    Flux<Blog> search(String query);
}

class BlogSearchRepositoryInternalImpl implements BlogSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    BlogSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Blog> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Blog.class).map(SearchHit::getContent);
    }
}
