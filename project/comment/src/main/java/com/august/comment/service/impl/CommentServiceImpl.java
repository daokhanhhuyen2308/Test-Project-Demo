package com.august.comment.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.RepliesPaginationFilter;
import com.august.comment.entity.elasticsearch.CommentDocument;
import com.august.comment.entity.mongodb.Comment;
import com.august.comment.mapper.CommentMapper;
import com.august.comment.repository.CommentESRepository;
import com.august.comment.repository.CommentRepository;
import com.august.comment.service.CommentService;
import com.august.shared.dto.AuthCurrentUser;
import com.august.shared.dto.PageResponse;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import com.august.shared.strategy.time.TimeUnitStrategy;
import com.august.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TimeUnitStrategy timeUnitStrategy;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${KEY.COMMENT.COUNT}")
    private String KEY_COMMENT_COUNT;
    private final ElasticsearchOperations elasticsearchOperations;
    private final CommentESRepository commentESRepository;
    private final SecurityUtils securityUtils;

    @Override
    public CommentResponse createCommentPost(CommentRequest request) {

        AuthCurrentUser currentUser = securityUtils.getCurrentUser();

        Comment comment = commentMapper.mapToComment(request);
        comment.setAuthorId(comment.getAuthorId());
        comment.setAuthorUsername(currentUser.getUsername());
        comment.setAuthorAvatarUrl(comment.getAuthorAvatarUrl());
        Comment savedComment = commentRepository.save(comment);

        String keyCmtCount = KEY_COMMENT_COUNT + request.getPostId();
        redisTemplate.opsForValue().increment(keyCmtCount);

        CommentDocument document = commentMapper.mapEntityToDoc(comment);
        commentESRepository.save(document);

        CommentResponse response = commentMapper.mapToCmtResponse(savedComment);
        response.setCreatedAt(timeUnitStrategy.processTimeUnitStrategy(comment.getCreatedAt()));

        return response;
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentReplies(RepliesPaginationFilter filter) {

        securityUtils.getCurrentUser();

        if (filter.getParentCmtId() == null || filter.getParentCmtId().isBlank()){
            throw new AppCustomException(ErrorCode.INPUT_REQUIREMENT);
        }

        Query query = Query.of(q -> q.bool(b -> {
            b.must(m -> m.term(t -> t.field("parentCommentId")
                    .value(FieldValue.of(filter.getParentCmtId()))));

            if (filter.getFromDate() != null || filter.getToDate() != null){
                b.must(m -> m.range(r -> r.date(d -> d.field("createdAd")
                        .gte(filter.getFromDate() != null ? filter.getFromDate().toString() : null)
                        .lte(filter.getToDate() != null ? filter.getToDate().toString() : null))));

            }
            return b;
        }));

        return processNativeQuery(query, filter, false);
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentsBySlug(RepliesPaginationFilter filter, String slug) {

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

    private PageResponse<CommentResponse> processNativeQuery(Query query, RepliesPaginationFilter filter, Boolean isSlug){
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
