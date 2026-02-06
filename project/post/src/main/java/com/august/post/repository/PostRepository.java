package com.august.post.repository;

import com.august.post.entity.mssql.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String> {
    Optional<PostEntity> findBySlug(String slug);

    @Modifying
    @Query("update PostEntity p set p.commentCount = :count where p.id = :postId")
    void updateCommentCount(@Param("count") Long count, @Param("postId") Long postId);

    @Modifying
    @Query("update PostEntity p set p.viewCount = :count where p.id = :postId")
    void updateViewCount(@Param("count") Long count, @Param("postId") Long postId);
}
