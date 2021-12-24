package com.okta.developer.blog.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.okta.developer.blog.IntegrationTest;
import com.okta.developer.blog.domain.Blog;
import com.okta.developer.blog.repository.BlogRepository;
import com.okta.developer.blog.repository.UserRepository;
import com.okta.developer.blog.repository.search.BlogSearchRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link BlogResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class BlogResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_HANDLE = "AAAAAAAAAA";
    private static final String UPDATED_HANDLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/blogs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/blogs";

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * This repository is mocked in the com.okta.developer.blog.repository.search test package.
     *
     * @see com.okta.developer.blog.repository.search.BlogSearchRepositoryMockConfiguration
     */
    @Autowired
    private BlogSearchRepository mockBlogSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Blog blog;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Blog createEntity() {
        Blog blog = new Blog().name(DEFAULT_NAME).handle(DEFAULT_HANDLE);
        return blog;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Blog createUpdatedEntity() {
        Blog blog = new Blog().name(UPDATED_NAME).handle(UPDATED_HANDLE);
        return blog;
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        blogRepository.deleteAll().block();
        blog = createEntity();
    }

    @Test
    void createBlog() throws Exception {
        int databaseSizeBeforeCreate = blogRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockBlogSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Blog
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeCreate + 1);
        Blog testBlog = blogList.get(blogList.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(DEFAULT_HANDLE);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(1)).save(testBlog);
    }

    @Test
    void createBlogWithExistingId() throws Exception {
        // Create the Blog with an existing ID
        blog.setId("existing_id");

        int databaseSizeBeforeCreate = blogRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeCreate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogRepository.findAll().collectList().block().size();
        // set the field null
        blog.setName(null);

        // Create the Blog, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkHandleIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogRepository.findAll().collectList().block().size();
        // set the field null
        blog.setHandle(null);

        // Create the Blog, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllBlogsAsStream() {
        // Initialize the database
        blogRepository.save(blog).block();

        List<Blog> blogList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Blog.class)
            .getResponseBody()
            .filter(blog::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(blogList).isNotNull();
        assertThat(blogList).hasSize(1);
        Blog testBlog = blogList.get(0);
        assertThat(testBlog.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(DEFAULT_HANDLE);
    }

    @Test
    void getAllBlogs() {
        // Initialize the database
        blogRepository.save(blog).block();

        // Get all the blogList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(blog.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].handle")
            .value(hasItem(DEFAULT_HANDLE));
    }

    @Test
    void getBlog() {
        // Initialize the database
        blogRepository.save(blog).block();

        // Get the blog
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, blog.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(blog.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.handle")
            .value(is(DEFAULT_HANDLE));
    }

    @Test
    void getNonExistingBlog() {
        // Get the blog
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewBlog() throws Exception {
        // Configure the mock search repository
        when(mockBlogSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        blogRepository.save(blog).block();

        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();

        // Update the blog
        Blog updatedBlog = blogRepository.findById(blog.getId()).block();
        updatedBlog.name(UPDATED_NAME).handle(UPDATED_HANDLE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedBlog.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedBlog))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);
        Blog testBlog = blogList.get(blogList.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(UPDATED_HANDLE);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository).save(testBlog);
    }

    @Test
    void putNonExistingBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, blog.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void putWithIdMismatchBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void putWithMissingIdPathParamBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void partialUpdateBlogWithPatch() throws Exception {
        // Initialize the database
        blogRepository.save(blog).block();

        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();

        // Update the blog using partial update
        Blog partialUpdatedBlog = new Blog();
        partialUpdatedBlog.setId(blog.getId());

        partialUpdatedBlog.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBlog.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBlog))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);
        Blog testBlog = blogList.get(blogList.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(DEFAULT_HANDLE);
    }

    @Test
    void fullUpdateBlogWithPatch() throws Exception {
        // Initialize the database
        blogRepository.save(blog).block();

        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();

        // Update the blog using partial update
        Blog partialUpdatedBlog = new Blog();
        partialUpdatedBlog.setId(blog.getId());

        partialUpdatedBlog.name(UPDATED_NAME).handle(UPDATED_HANDLE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBlog.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBlog))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);
        Blog testBlog = blogList.get(blogList.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(UPDATED_HANDLE);
    }

    @Test
    void patchNonExistingBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, blog.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void patchWithIdMismatchBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void patchWithMissingIdPathParamBlog() throws Exception {
        int databaseSizeBeforeUpdate = blogRepository.findAll().collectList().block().size();
        blog.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(blog))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Blog in the database
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(0)).save(blog);
    }

    @Test
    void deleteBlog() {
        // Configure the mock search repository
        when(mockBlogSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockBlogSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        blogRepository.save(blog).block();

        int databaseSizeBeforeDelete = blogRepository.findAll().collectList().block().size();

        // Delete the blog
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, blog.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Blog> blogList = blogRepository.findAll().collectList().block();
        assertThat(blogList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Blog in Elasticsearch
        verify(mockBlogSearchRepository, times(1)).deleteById(blog.getId());
    }

    @Test
    void searchBlog() {
        // Configure the mock search repository
        when(mockBlogSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        blogRepository.save(blog).block();
        when(mockBlogSearchRepository.search("id:" + blog.getId())).thenReturn(Flux.just(blog));

        // Search the blog
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + blog.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(blog.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].handle")
            .value(hasItem(DEFAULT_HANDLE));
    }
}
