package com.august.profile.repository;

import com.august.profile.entity.UserFollow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    @Modifying
    @Query(value = """
    insert into user_follow (follower_id, following_id, created_at)
    values (:followerId, :followingId, now())
    on duplicate key update created_at = now()
    """, nativeQuery = true)
    void upsertFollow(@Param("followerId") String followerId,
                      @Param("followingId") String followingId);


    @Modifying
    @Query(value = """
    delete from user_follow
    where follower_id = :followerId and following_id = :followingId
    """, nativeQuery = true)
    void deleteFollow(@Param("followerId") String followerId,
                      @Param("followingId") String followingId);

    @Query(value = """
    select u from UserFollow u where u.followingId = :authorId
    and (
    :lastCreatedAt is null
    or u.createdAt < :lastCreatedAt
    or (u.createdAt = :lastCreatedAt and u.id < :lastId)
    )
    order by u.createdAt desc, u.id desc
    """)
    List<UserFollow> findFollowersBySearchAfter(@Param("authorId") String authorId,
                                                @Param("lastCreatedAt") Instant lastCreatedAt,
                                                @Param("lastId") String lastId,
                                                Pageable pageable);

    @Query("""
    select u from UserFollow u where u.followerId = :authorId
    and (
    :lastCreatedAt is null
    or u.createdAt < :lastCreatedAt
    or (u.createdAt = :lastCreatedAt and u.id < :lastId)
    )
    order by u.createdAt desc, u.id desc
    """)
    List<UserFollow> findFollowingsBySearchAfter(@Param("authorId") String authorId,
                                                 @Param("lastCreatedAt") Instant lastCreatedAt,
                                                 @Param("lastId") String lastId,
                                                 Pageable pageable);

    @Query(value = "select u.followingId from UserFollow u " +
            "where u.followerId = :followerId " +
            "and u.followingId in :targetProfileIds")
    Set<String> findFollowingIdsByFollowerIdAndFollowingIdIn(@Param("followerId") String followerId,
                                                             @Param("targetProfileIds") List<String> targetProfileIds);
}
