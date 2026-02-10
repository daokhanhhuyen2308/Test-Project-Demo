package com.august.comment.repository;

import com.august.comment.entity.elasticsearch.CommentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentESRepository extends ElasticsearchRepository<CommentDocument, String> {
}
