package com.august.comment.mapper;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.entity.elasticsearch.CommentDocument;
import com.august.comment.entity.mongodb.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    //entity -> response
    @Mappings({
        @Mapping(target = "author.authorId", source = "authorId"),
        @Mapping(target = "author.authorUsername", source = "authorUsername"),
        @Mapping(target = "author.authorAvatarUrl", source = "authorAvatarUrl"),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "replyCount", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "replies", ignore = true)
    })
    CommentResponse mapToCmtResponse(Comment comment);

    //request -> entity
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "authorId", ignore = true),
        @Mapping(target = "authorUsername", ignore = true),
        @Mapping(target = "authorAvatarUrl", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "postSlug", ignore = true),
        @Mapping(target = "replyCount", ignore = true)
    })
    Comment mapToComment(CommentRequest request);

    //entity -> elastic
    @Mappings({
            @Mapping(target = "author.authorId", source = "authorId"),
            @Mapping(target = "author.authorUsername", source = "authorUsername"),
            @Mapping(target = "author.authorAvatarUrl", source = "authorAvatarUrl"),
    })
    CommentDocument mapEntityToDoc(Comment comment);

    //elastic -> response
    @Mappings({
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "replies", ignore = true),
    })
    CommentResponse mapDocToResponse(CommentDocument document);

}
