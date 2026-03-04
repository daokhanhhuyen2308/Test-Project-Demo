package com.august.post.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.august.post.repository.elastic.PostElasticSearchRepo;
import com.august.protocol.profile.FileServiceGrpc;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.dto.PageResponse;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.august.sharecore.strategy.time.TimeUnitStrategy;
import com.august.sharesecurity.dto.AuthCurrentUser;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.post.entity.elastic.PostDocument;
import com.august.post.entity.mssql.CategoryEntity;
import com.august.post.entity.mssql.PostEntity;
import com.august.post.entity.mssql.TagEntity;
import com.august.post.mapper.PostMapper;
import com.august.post.repository.jpa.CategoryRepository;
import com.august.post.repository.jpa.PostRepository;
import com.august.post.repository.jpa.TagRepository;
import com.august.post.service.PostService;
import com.august.post.utils.SlugUtils;
import com.august.sharesecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostElasticSearchRepo postElasticSearchRepo;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final TimeUnitStrategy timeUnitStrategy;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${KEY.VIEW.COUNT}")
    private String KEY_VIEW_COUNT;
    private final SecurityUtils securityUtils;
    @GrpcClient("file-service-grpc")
    private final FileServiceGrpc.FileServiceBlockingStub fileStub;

    @Override
    public ApiResponse<PostResponse> createPost(PostCreationRequest request) {
        System.out.println("Service implement");

        AuthCurrentUser currentUser = securityUtils.getCurrentUser();

        PostEntity postEntity = postMapper.mapToPostEntity(request);
        postEntity.setSlug(SlugUtils.slug(request.getTitle(), true));
        postEntity.setAuthorKeycloakId(currentUser.getKeycloakId());
        postEntity.setAuthorUsername(currentUser.getUsername());
        postEntity.setAuthorAvatarUrl(currentUser.getAvatarUrl());

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<TagEntity> tagEntities = processTags(request.getTags());
            postEntity.setTags(tagEntities);
        }
        postEntity.setReadingTime(calculateReadingTime(request.getContent()));
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppCustomException(ErrorCode.CATEGORY_NOT_FOUND));
        postEntity.setCategory(category);

        postRepository.save(postEntity);

        try {
            PostDocument postDocument = postMapper.mapToDocument(postEntity);
            postElasticSearchRepo.save(postDocument);

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
                        .fields("author.authorUsername^4", "title^3",
                                        "summary^2", "content", "tags.name", "category.name")
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .minimumShouldMatch("1")));
            }

            //Author filter
            if (StringUtils.hasText(query.getAuthorUsername())){
                b.must(m -> m.term(t -> t.field("author.authorUsername")
                        .value(query.getAuthorUsername())));
            }

            //Date range
            if (query.getFromDate() != null || query.getToDate() != null){

                LocalDateTime from = query.getFromDate() != null
                        ? query.getFromDate().atStartOfDay()
                        : null;

                LocalDateTime to = query.getToDate() != null
                        ? query.getToDate().atTime(LocalTime.MAX)
                        : null;

                b.must(m -> m.range(r -> r.field("createdAt")
                        .gte(query.getFromDate() != null ? JsonData.of(from) : null)
                        .lte(query.getToDate() != null ? JsonData.of(to) : null)));
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
                .map(hit -> {
                    PostResponse response = postMapper.mapDocToResponse(hit.getContent());
                    response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(hit.getContent().getCreatedAt()));
                    return response;
                })
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
        apiResponse.setStatus(200);
        return apiResponse;
    }

    @Override
    public ApiResponse<PostResponse> getPostBySlug(String slug) {
        PostEntity postEntity = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppCustomException(ErrorCode.NOT_FOUND_POST_BY_SLUG));
        String key = KEY_VIEW_COUNT + "_" + postEntity.getId();
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofHours(24));
        return mapToPostResponse(postEntity);
    }

    @Override
    public List<PostResponse> getRelatedPosts(Long postId) {

        Query query = Query.of(q -> q.moreLikeThis(m -> m
                .fields("title", "summary", "tags.name", "content", "category.name")
                .like(l -> l.document(d -> d
                        .index("post_index")
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

    @Override
    public PostResponse uploadThumbnail(String postId, MultipartFile file) {
        return null;
    }

    private int calculateReadingTime(String content){
        if (content == null || content.isEmpty()) return 0;
        String[] words = content.trim().split("\\s+");
        int wordCount = words.length;
        return (int) Math.ceil(wordCount / 100.0);
    }

    private List<TagEntity> processTags(List<String> tags){
        return tags.stream().map(tag -> {
            String tagSlug = SlugUtils.slug(tag, false);
            return tagRepository.findBySlug(tagSlug)
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setName(tag);
                        newTag.setSlug(tagSlug);
                        return tagRepository.save(newTag);
                    });
        }).toList();
    }

    private ApiResponse<PostResponse> mapToPostResponse(PostEntity entity){
        ApiResponse<PostResponse> response = new ApiResponse<>();
        PostResponse postResponse = postMapper.mapToPostResponse(entity);
        postResponse.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(entity.getCreatedAt()));
        response.setResult(postResponse);
        response.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode());
        response.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
        response.setStatus(200);
        return response;
    }

}
