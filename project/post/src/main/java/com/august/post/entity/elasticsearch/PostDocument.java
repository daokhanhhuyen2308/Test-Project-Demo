package com.august.post.entity.elasticsearch;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Document(indexName = "post_index")
public class PostDocument {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String authorUsername;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String slug;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String summary;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String content;
    @Field(type = FieldType.Keyword)
    private String categoryName;
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;
}
