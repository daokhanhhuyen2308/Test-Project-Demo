package com.august.post.repository.elastic;

import com.august.post.entity.elastic.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostElasticSearchRepo extends ElasticsearchRepository<PostDocument, String> {
}
