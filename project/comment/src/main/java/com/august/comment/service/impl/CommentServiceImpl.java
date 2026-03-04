package com.august.comment.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
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

        Query query = Query.of(q -> q.bool(b -> {
            b.must(m -> m.term(t -> t.field("parentCommentId")
                    .value(FieldValue.of(filter.getParentCmtId()))));

            if (filter.getFromDate() != null || filter.getToDate() != null){
                LocalDateTime from = filter.getFromDate() != null
                        ? filter.getFromDate().atStartOfDay()
                        : null;

                LocalDateTime to = filter.getToDate() != null
                        ? filter.getToDate().atTime(LocalTime.MAX)
                        : null;

                b.must(m -> m.range(r -> r.field("createdAt")
                        .gte(filter.getFromDate() != null ? JsonData.of(from) : null)
                        .lte(filter.getToDate() != null ? JsonData.of(to) : null)));
            }
            return b;
        }));

        return processNativeQuery(query, filter, false);
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentsBySlug(CommentPaginationFilter filter, String slug) {

        if (slug == null || slug.isBlank()){
            throw new AppCustomException(ErrorCode.SLUG_IS_REQUIRED);
        }

        Query query = Query.of(q -> q
                .bool(b -> b
                        .must(m -> m
                                .term(t -> t
                                        .field("postSlug").value(FieldValue.of(slug))))
                        .mustNot(mn -> mn.exists(e -> e.field("parentCommentId")))));

        return processNativeQuery(query, filter, true);
    }

    private PageResponse<CommentResponse> processNativeQuery(Query query, CommentPaginationFilter filter,
                                                             Boolean isSlug){
        Sort.Direction direction = filter.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC;

        NativeQuery nativeQuery =  NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(filter.getPage(), filter.getSize())
                        .withSort(Sort.by(direction, "createdAd")
                                .and(Sort.by(Sort.Direction.ASC, "id"))))
                .build();

        if (filter.getSearchAfter() != null && filter.getSearchAfter().length > 0){
            nativeQuery.setSearchAfter(List.of(filter.getSearchAfter()));
        }

        SearchHits<CommentDocument> searchHits = elasticsearchOperations.search(nativeQuery, CommentDocument.class);

        Object[] nextCursor = null;
        if (searchHits.hasSearchHits()){
            SearchHit<CommentDocument> hit = searchHits.getSearchHit(searchHits.getSearchHits().size() - 1);
            nextCursor = hit.getSortValues().toArray();
        }

        List<CommentResponse> content = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .map(hit -> {
                    if (isSlug){
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
                .build();
    }
}
