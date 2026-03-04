package com.august.profile.repository;

import com.august.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, String> {

    @Query("select p from UserProfile p where p.keycloakId = :keycloakId")
    UserProfile findByKeycloakId(@Param("keycloakId") String keycloakId);

}
