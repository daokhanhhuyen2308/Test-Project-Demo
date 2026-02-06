package com.august.post.mapper;

import com.august.post.dto.CategoryResponse;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostResponse;
import com.august.post.dto.TagResponse;
import com.august.post.entity.elasticsearch.PostDocument;
import com.august.post.entity.mssql.CategoryEntity;
import com.august.post.entity.mssql.PostEntity;
import com.august.post.entity.mssql.TagEntity;
import com.august.post.utils.SlugUtils;
import com.august.protocol.post.IncreaseCommentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorUsername", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "readingTime", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "isPaid", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "slug", ignore = true)
    PostEntity mapToPostEntity(PostCreationRequest request);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapToTagsResponse")
    @Mapping(source = "category", target = "category", qualifiedByName = "mapToCategoryResponse")
    PostResponse mapToPostResponse(PostEntity entity);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapToTagString")
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapToCategoryName")
    PostDocument mapToDocument(PostEntity postEntity);

    PostResponse mapDocToResponse(PostDocument document);


    PostEntity mapPostGrpcTpEntity(IncreaseCommentRequest request);

    @Named("mapToTagString")
    default List<String> mapToTagString(List<TagEntity> tagEntities){
        return tagEntities.stream().map(TagEntity::getTagName).toList();
    }

    @Named("mapToCategoryName")
    default String mapToCategoryName(CategoryEntity category){
        return category.getCategoryName();
    }

    @Named("mapToTagsResponse")
    default List<TagResponse> mapToTagsResponse(List<TagEntity> tags){
        if (tags == null){
            return Collections.emptyList();
        }
        return tags.stream().map(tag -> TagResponse.builder()
                .id(tag.getId())
                .tag(tag.getTagName())
                .slug(SlugUtils.slug(tag.getTagName(), false))
                .build()).toList();
    }

    @Named("mapToCategoryResponse")
    default CategoryResponse mapToCategoryResponse(CategoryEntity category){
        return category != null ? CategoryResponse.builder()
                .id(category.getId()).name(category.getCategoryName())
                .slug(SlugUtils.slug(category.getCategoryName(), false))
                .build() : null;
    }
}
