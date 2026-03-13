package com.august.comment.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.CommentPaginationFilter;
import com.august.comment.entity.elastic.CommentDocument;
import com.august.comment.entity.mongodb.Comment;
import com.august.comment.mapper.CommentMapper;
import com.august.comment.repository.elastic.CommentESRepository;
import com.august.comment.repository.jpa.CommentRepository;
import com.august.comment.service.CommentService;
import com.august.sharecore.dto.PageResponse;
import com.august.sharecore.dto.SearchAfterCursor;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.august.sharecore.strategy.time.TimeUnitStrategy;
import com.august.sharesecurity.dto.AuthCurrentUser;
import com.august.sharesecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TimeUnitStrategy timeUnitStrategy;
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${KEY.COMMENT.COUNT}")
    private String KEY_COMMENT_COUNT;
    private final ElasticsearchOperations elasticsearchOperations;
    private final CommentESRepository commentESRepository;
    private final SecurityUtils securityUtils;

    @Override
    public CommentResponse createCommentPost(CommentRequest request) {

        AuthCurrentUser currentUser = securityUtils.getCurrentUser();

        Comment comment = commentMapper.mapToComment(request);
        comment.setAuthorId(currentUser.getKeycloakId());
        comment.setAuthorUsername(currentUser.getUsername());
        comment.setAuthorAvatarUrl(currentUser.getAvatarUrl());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        String keyCmtCount = KEY_COMMENT_COUNT + "_" + request.getPostId();
        stringRedisTemplate.opsForValue().increment(keyCmtCount);
        stringRedisTemplate.expire(keyCmtCount, Duration.ofHours(24));

        CommentDocument document = commentMapper.mapEntityToDoc(savedComment);
        commentESRepository.save(document);

        CommentResponse response = commentMapper.mapToCmtResponse(savedComment);
        response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(savedComment.getCreatedAt()));

        return response;
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentReplies(CommentPaginationFilter filter) {

        securityUtils.getCurrentUser();

        if (filter.getParentCmtId() == null || filter.getParentCmtId().isBlank()){
            throw new AppCustomException(ErrorCode.INPUT_REQUIREMENT);
        }

        Query query = buildBaseCommentQuery(filter, null);

        return processNativeQuery(query, filter, false);
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentsByPostId(CommentPaginationFilter filter, Long postId) {

        Query query = buildBaseCommentQuery(filter, postId);

        return processNativeQuery(query, filter, true);
    }

    private void validateParentCommentId(CommentPaginationFilter filter, BoolQuery.Builder b) {
        if (filter.getParentCmtId() != null && !filter.getParentCmtId().isBlank()) {
            b.must(m -> m
                    .term(t -> t.field("parentCommentId").value(String.valueOf(filter.getParentCmtId())))
            );
        } else {
            b.mustNot(mn -> mn.exists(e -> e.field("parentCommentId")));
        }
    }

    private Query buildBaseCommentQuery(CommentPaginationFilter filter, Long postId) {
        return Query.of(q -> q.bool(b -> {

            System.out.println("Here 1");

            if (postId != null) {
                b.must(m -> m.term(t -> t.field("postId").value(String.valueOf(postId))));
            }

            System.out.println("Here 2");

            validateParentCommentId(filter, b);

            System.out.println("Here 3");

            if (filter.getFromDate() != null || filter.getToDate() != null) {
                b.must(m -> m.range(r -> {
                    r.field("createdAt");
                    if (filter.getFromDate() != null) {
                        System.out.println("Here 4");
                        r.gte(JsonData.of(filter.getFromDate().atStartOfDay()));
                    }
                    if (filter.getToDate() != null) {
                        System.out.println("Here 5");
                        r.lte(JsonData.of(filter.getToDate().atTime(LocalTime.MAX)));
                    }
                    return r;
                }));
            }

            return b;
        }));

    }

    private PageResponse<CommentResponse> processNativeQuery(Query query, CommentPaginationFilter filter,
                                                             Boolean isPostId){
        Sort.Direction direction = filter.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC;

        NativeQuery nativeQuery =  NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(filter.getPage(), filter.getSize() + 1)
                        .withSort(Sort.by(direction, "createdAt")
                                .and(Sort.by(Sort.Direction.ASC, "id"))))
                .build();

        if (filter.getSearchAfter() != null){
            Object[] rawValues = new Object[] {
                    filter.getSearchAfter().getLastCreatedAt(),
                    filter.getSearchAfter().getLastId()};
            nativeQuery.setSearchAfter(List.of(rawValues));
        }

        SearchHits<CommentDocument> searchHits = elasticsearchOperations.search(nativeQuery, CommentDocument.class);

        List<SearchHit<CommentDocument>> searchHitList = searchHits.getSearchHits();

        boolean hasMore = searchHitList.size() > filter.getSize();

        SearchAfterCursor nextCursor = null;
        if (!searchHitList.isEmpty()) {
            SearchHit<CommentDocument> lastHit = searchHitList.getLast();
            nextCursor = new SearchAfterCursor()
                    .parseObjectToSearchAfterObject(lastHit.getSortValues().toArray(), LocalDateTime.class);
        }

        List<CommentResponse> content = searchHits.getSearchHits()
                .stream()
                .limit(filter.getSize())
                .map(SearchHit::getContent)
                .map(hit -> {
                    if (isPostId){
                        CommentResponse response = commentMapper.mapDocToResponse(hit);
                        response.setReplies(null);
                        response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(hit.getCreatedAt()));
                        return response;
                    }
                    return commentMapper.mapDocToResponse(hit);

                })
                .toList();

        return PageResponse.<CommentResponse>builder()
                .content(content)
                .pageSize(filter.getSize())
                .totalElements(searchHits.getTotalHits())
                .nextSearchAfter(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
