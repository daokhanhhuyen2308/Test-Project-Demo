package com.august.post.repository.jpa;

import com.august.post.entity.mssql.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findBySlug(String slug);

    @Transactional
    @Modifying
    @Query("update PostEntity p set p.viewCount = p.viewCount + :viewInc, p.commentCount = p.commentCount + :commentInc" +
            " where p.id = :postId")
    void updateCounts(@Param("postId") Long postId,
                      @Param("viewInc") Long viewInc,
                      @Param("commentInc") Long commentInc);
}
