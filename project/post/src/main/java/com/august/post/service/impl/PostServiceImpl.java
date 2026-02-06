package com.august.post.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.august.post.dto.PageResponse;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.post.entity.elasticsearch.PostDocument;
import com.august.post.entity.mssql.CategoryEntity;
import com.august.post.entity.mssql.PostEntity;
import com.august.post.entity.mssql.TagEntity;
import com.august.post.mapper.PostMapper;
import com.august.post.repository.CategoryRepository;
import com.august.post.repository.PostElasticSearchRepo;
import com.august.post.repository.PostRepository;
import com.august.post.repository.TagRepository;
import com.august.post.service.PostService;
import com.august.post.utils.SlugUtils;
import com.august.shared.dto.ApiResponse;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import com.august.shared.strategy.TimeStrategyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostElasticSearchRepo elasticSearchRepo;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final TimeStrategyContext timeStrategyContext;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${KEY.VIEW.COUNT}")
    private String KEY_VIEW_COUNT;

    @Override
    public ApiResponse<PostResponse> createPost(PostCreationRequest request, Jwt jwt) {

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        PostEntity postEntity = postMapper.mapToPostEntity(request);
        postEntity.setSlug(SlugUtils.slug(request.getTitle(), true));
        postEntity.setAuthorId(userId);
        postEntity.setAuthorUsername(username);
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<TagEntity> tagEntities = processTags(request.getTags());
            postEntity.setTags(tagEntities);
        }
        postEntity.setReadingTime(calculateReadingTime(request.getContent()));
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppCustomException(ErrorCode.CATEGORY_NOT_FOUND));
        postEntity.setCategory(category);

        postRepository.save(postEntity);

        try{
        PostDocument postDocument = postMapper.mapToDocument(postEntity);
        elasticSearchRepo.save(postDocument);

        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.DO_NOT_CONNECT_TO_ELASTICSEARCH);
        }

        return mapToPostResponse(postEntity);
    }

    @Override
    public ApiResponse<PageResponse<PostResponse>> searchPosts(PostPaginationFilter query) {

        Query esQuery = Query.of(q -> q.bool(b -> {

            //Keyword search
            if (StringUtils.hasText(query.getKeyword())){
                b.should(s -> s.multiMatch(m -> m
                        .query(query.getKeyword())
                        .fields("authorUsername^4", "title^3", "summary^2", "content", "tags", "category")
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .minimumShouldMatch("1")));
            }

            //Author filter
            if (StringUtils.hasText(query.getAuthorUsername())){
                b.must(m -> m.term(t -> t.field("authorUsername.keyword")
                        .value(query.getAuthorUsername())));
            }

            //Date range
            if (query.getFromDate() != null || query.getToDate() != null){
                b.must(m -> m.range(r -> r.date(d -> d.field("createdAt")
                        .gte(query.getFromDate() != null ? query.getFromDate().toString() : null)
                        .lte(query.getToDate() != null ? query.getToDate().toString() : null))));
            }
            return b;

        }));

        Sort.Direction direction = query.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC;

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withPageable(PageRequest.of(query.getPage(), query.getSize()))
                .withSort(Sort.by(direction, "createdAt"
                ).and(Sort.by(Sort.Direction.ASC, "id")))
                .build();

        if (query.getSearchAfter() != null && query.getSearchAfter().length > 0){
            nativeQuery.setSearchAfter(List.of(query.getSearchAfter()));
        }

        //execute query based on keyword
        SearchHits<PostDocument> searchHits = elasticsearchOperations.search(nativeQuery, PostDocument.class);

        Object[] nextSearchAfter = null;
        if (searchHits.hasSearchHits()){
            SearchHit<PostDocument> lastHit = searchHits.getSearchHit(searchHits.getSearchHits().size() - 1);
            nextSearchAfter = lastHit.getSortValues().toArray();
        }
        List<PostResponse> content = searchHits.stream()
                .map(hit -> postMapper.mapDocToResponse(hit.getContent()))
                .toList();

        ApiResponse<PageResponse<PostResponse>> apiResponse = new ApiResponse<>();

        PageResponse<PostResponse> pageResponse = PageResponse.<PostResponse>builder()
                .content(content)
                .pageSize(query.getSize())
                .totalElements(searchHits.getTotalHits())
                .nextSearchAfter(nextSearchAfter)
                .build();

        apiResponse.setResult(pageResponse);
        apiResponse.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode());
        apiResponse.setMessage(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
        return apiResponse;

    }
    @Override
    public ApiResponse<PostResponse> getPostBySlug(String slug) {
        PostEntity postEntity = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppCustomException(ErrorCode.NOT_FOUND_POST_BY_SLUG));
        redisTemplate.opsForValue().increment(KEY_VIEW_COUNT +postEntity.getId(), 1);
        return mapToPostResponse(postEntity);
    }

    @Override
    public List<PostResponse> getRelatedPosts(Long postId) {

        Query query = Query.of(q -> q.moreLikeThis(m -> m
                .fields("title", "summary", "tags", "content", "category")
                .like(l -> l.document(d -> d
                        .index("posts_index")
                        .id(postId.toString())))
                .minDocFreq(1)
                .maxDocFreq(1)
                .minTermFreq(1)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<PostDocument> searchHits = elasticsearchOperations.search(nativeQuery, PostDocument.class);

        return searchHits.stream()
                .map(hit -> postMapper.mapDocToResponse(hit.getContent()))
                .toList();
    }


    private int calculateReadingTime(String content){
        if (content == null || content.isEmpty()) return 0;
        int wordCount = content.trim().split("\\s+").length;
        return (int) Math.ceil((double) wordCount / 200);
    }

    private List<TagEntity> processTags(List<String> tags){
        return tags.stream().map(tag -> {
            String tagSlug = SlugUtils.slug(tag, false);

            return tagRepository.findBySlug(tagSlug)
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setTagName(tag);
                        newTag.setSlug(tagSlug);
                        return tagRepository.save(newTag);
                    });
        }).toList();
    }

    private ApiResponse<PostResponse> mapToPostResponse(PostEntity entity){
        ApiResponse<PostResponse> response = new ApiResponse<>();
        PostResponse postResponse = postMapper.mapToPostResponse(entity);
        postResponse.setCreatedAt(timeStrategyContext.executeStrategy(entity.getCreatedAt()));
        response.setResult(postResponse);
        response.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode());
        response.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
        return response;
    }
}
