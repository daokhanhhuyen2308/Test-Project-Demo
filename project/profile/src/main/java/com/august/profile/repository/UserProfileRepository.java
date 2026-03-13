package com.august.profile.repository;

import com.august.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    @Query("select p from UserProfile p where p.keycloakId = :keycloakId")
    UserProfile findByKeycloakId(@Param("keycloakId") String keycloakId);

    boolean existsByKeycloakId(String keycloakId);

    @Query("select p from UserProfile p where p.username = :username")
    Optional<UserProfile> findByUsername(@Param("username") String username);

    @Modifying
    @Query(value = "update UserProfile p set p.keycloakId = :keycloakId, p.followerCount = :count", nativeQuery = true)
    void updateFollowerCount(@Param("keycloakId") String keycloakId,
                             @Param("count") Long count);


    @Modifying
    @Query(value = "update UserProfile p set p.keycloakId = :keycloakId, p.followingCount = :count", nativeQuery = true)
    void updateFollowingCount(@Param("keycloakId") String keycloakId,
                              @Param("count") Long count);
}
