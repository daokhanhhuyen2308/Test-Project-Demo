package com.august.post.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.august.post.repository.elastic.PostElasticSearchRepo;
import com.august.protocol.file.FilePurpose;
import com.august.protocol.file.FileServiceGrpc;
import com.august.protocol.file.UploadFileRequest;
import com.august.protocol.file.UploadFileResponse;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.dto.PageResponse;
import com.august.sharecore.dto.SearchAfterCursor;
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
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private FileServiceGrpc.FileServiceBlockingStub fileStub;
    private static final String FAVORITE_KEY_PREFIX = "POST:FAVORITE:";

    @Override
    @Transactional
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
    public PageResponse<PostResponse> searchPosts(PostPaginationFilter query) {

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

            // Category filter (Isolation logic)
            if (StringUtils.hasText(query.getCategory())) {
                b.must(m -> m.term(t -> t.field("category.name.keyword")
                        .value(query.getCategory())));
            }

            if (StringUtils.hasText(query.getTag())) {
                b.must(m -> m.term(t -> t.field("tags.name.keyword")
                        .value(query.getTag())));
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
                .withPageable(PageRequest.of(query.getPage(), query.getSize() + 1))
                .withSort(Sort.by(direction, "createdAt"
                ).and(Sort.by(Sort.Direction.ASC, "id")))
                .build();


        //    Object[] searchAfter = new Object[] {
        //    cursor.getLastSortValue(), // Ví dụ: 2026-03-12T15:00:00 (LocalDateTime)
        //    cursor.getLastId()         // Ví dụ: "comment-666"
        //};
        if (query.getSearchAfter() != null && query.getSearchAfter().length > 0){
            nativeQuery.setSearchAfter(List.of(query.getSearchAfter()));
        }

        //execute query based on keyword
        SearchHits<PostDocument> searchHits = elasticsearchOperations.search(nativeQuery, PostDocument.class);

        List<SearchHit<PostDocument>> searchHitList = searchHits.getSearchHits();

        boolean hasMore = searchHitList.size() > query.getSize();

        SearchAfterCursor nextSearchAfter = null;
        if (!searchHitList.isEmpty()){
            SearchHit<PostDocument> lastHit = searchHitList.getLast();
            nextSearchAfter = new SearchAfterCursor()
                    .parseObjectToSearchAfterObject(lastHit.getSortValues().toArray(), LocalDateTime.class);
        }
        List<PostResponse> content = searchHits.stream()
                .map(hit -> {
                    PostResponse response = postMapper.mapDocToResponse(hit.getContent());
                    response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(hit.getContent().getCreatedAt()));
                    return response;
                })
                .toList();

        return PageResponse.<PostResponse>builder()
                .content(content)
                .pageSize(query.getSize())
                .totalElements(searchHits.getTotalHits())
                .nextSearchAfter(nextSearchAfter)
                .hasMore(hasMore)
                .build();
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

    @Transactional
    @Override
    public PostResponse uploadThumbnail(Long postId, MultipartFile thumbnail) {
        try {
            AuthCurrentUser currentUser = securityUtils.getCurrentUser();

            PostEntity postEntity = postRepository.findById(postId)
                    .orElseThrow(() -> new AppCustomException(ErrorCode.POST_NOT_FOUND_BY_ID, postId));

            if (!postEntity.getAuthorKeycloakId().equals(currentUser.getKeycloakId())) {
                log.warn("User {} tried to update thumbnail of post {} owned by {}",
                        currentUser.getKeycloakId(), postId, postEntity.getAuthorKeycloakId());
                throw new AppCustomException(ErrorCode.UNAUTHORIZED_UPDATE_POST);
            }

            UploadFileRequest request = UploadFileRequest.newBuilder()
                    .setFile(ByteString.copyFrom(thumbnail.getBytes()))
                    .setFileName(thumbnail.getOriginalFilename())
                    .setContentType(thumbnail.getContentType())
                    .setOwnerId(currentUser.getKeycloakId())
                    .setPurpose(FilePurpose.THUMBNAIL)
                    .build();

            UploadFileResponse response = fileStub.uploadFile(request);

            String thumbnailUrl = response.getUrl();
            postEntity.setThumbnail(thumbnailUrl);
            postRepository.save(postEntity);

            return postMapper.mapToPostResponse(postEntity);

        } catch (IOException e) {
            throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public PostResponse toggleFavorite(Long postId) {

        AuthCurrentUser currentUser = securityUtils.getCurrentUser();

        String userKeycloakId = currentUser.getKeycloakId();
        String redisKey = FAVORITE_KEY_PREFIX + postId;
        boolean isFavorited;

        //check
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(redisKey, userKeycloakId);

        if (Boolean.TRUE.equals(isMember)){
            stringRedisTemplate.opsForSet().remove(redisKey, userKeycloakId);
            log.info("User {} unfavorited post {}", userKeycloakId, postId);
            isFavorited = false;
        }
        else{
            stringRedisTemplate.opsForSet().add(redisKey, userKeycloakId);
            log.info("User {} favorited post {}", userKeycloakId, postId);
            isFavorited = true;
        }

        //update favorite count
        Long currentCount = stringRedisTemplate.opsForSet().size(redisKey);
        long countValue = (currentCount != null) ? currentCount : 0;

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppCustomException(ErrorCode.POST_NOT_FOUND_BY_ID));
        postEntity.setFavoriteCount(currentCount);
        updateFavoriteCountElasticSearch(postId, countValue);

        PostResponse response = postMapper.mapToPostResponse(postEntity);
        response.setIsFavorited(isFavorited);
        response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(postEntity.getCreatedAt()));

        return response;
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
        PostResponse postResponse = postMapper.mapToPostResponse(entity);
        postResponse.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(entity.getCreatedAt()));
        return ApiResponse.success(postResponse, ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
    }

    private void updateFavoriteCountElasticSearch(Long postId, Long countValue){

        UpdateQuery updateQuery = UpdateQuery.builder(postId.toString())
                .withDocument(Document.create().append("favoriteCount", countValue))
                .build();
        try{
            elasticsearchOperations.update(updateQuery, IndexCoordinates.of("post_index"));
        } catch (Exception e) {
            log.error("Failed to update ES for post {}: {}", postId, e.getMessage());
        }

    }

}
