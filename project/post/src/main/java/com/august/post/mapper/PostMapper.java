package com.august.post.mapper;

import com.august.post.dto.*;
import com.august.post.entity.elasticsearch.PostDocument;
import com.august.post.entity.mssql.CategoryEntity;
import com.august.post.entity.mssql.PostEntity;
import com.august.post.entity.mssql.TagEntity;
import com.august.post.utils.SlugUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    //request -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorUsername", ignore = true)
    @Mapping(target = "authorAvatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "readingTime", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    PostEntity mapToPostEntity(PostCreationRequest request);

    //entity -> response
    @Mappings({
                @Mapping(source = "authorId", target = "author.authorId"),
                @Mapping(source = "authorUsername", target = "author.authorUsername"),
                @Mapping(source = "authorAvatarUrl", target = "author.authorAvatarUrl"),
                @Mapping(source = "tags", target = "tags", qualifiedByName = "mapToTagsResponse"),
                @Mapping(source = "category", target = "category", qualifiedByName = "mapToCategoryResponse"),
                @Mapping(target = "createdAt", ignore = true)
    })
    PostResponse mapToPostResponse(PostEntity entity);

    //entity -> elastic
    @Mapping(source = "authorId", target = "author.authorId")
    @Mapping(source = "authorUsername", target = "author.authorUsername")
    @Mapping(source = "authorAvatarUrl", target = "author.authorAvatarUrl")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "id", expression = "java(String.valueOf(postEntity.getId()))")
    PostDocument mapToDocument(PostEntity postEntity);

    //elastic -> response
    @Mappings({
            @Mapping(source = "author.authorId", target = "author.authorId"),
            @Mapping(source = "author.authorUsername", target = "author.authorUsername"),
            @Mapping(source = "author.authorAvatarUrl", target = "author.authorAvatarUrl"),
            @Mapping(target = "createdAt", ignore = true),
    })
    PostResponse mapDocToResponse(PostDocument document);

    @Named("mapToTagsResponse")
    default List<TagResponse> mapToTagsResponse(List<TagEntity> tags){
        if (tags == null){
            return Collections.emptyList();
        }
        return tags.stream().map(tag -> TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(SlugUtils.slug(tag.getName(), false))
                .build()).toList();
    }


    @Named("mapToCategoryResponse")
    default CategoryResponse mapToCategoryResponse(CategoryEntity category){
        return category != null ? CategoryResponse.builder()
                .id(category.getId()).name(category.getName())
                .slug(SlugUtils.slug(category.getName(), false))
                .build() : null;
    }

}
