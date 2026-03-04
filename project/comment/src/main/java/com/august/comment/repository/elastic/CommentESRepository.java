package com.august.comment.repository.elastic;

import com.august.comment.entity.elastic.CommentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentESRepository extends ElasticsearchRepository<CommentDocument, String> {
}
