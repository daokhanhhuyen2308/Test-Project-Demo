package com.august.post.repository;

import com.august.post.entity.elasticsearch.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostElasticSearchRepo extends ElasticsearchRepository<PostDocument, String> {
}
