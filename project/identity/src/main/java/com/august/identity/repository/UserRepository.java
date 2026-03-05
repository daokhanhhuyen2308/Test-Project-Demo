package com.august.identity.repository;

import com.august.identity.entity.UserEntity;
import com.august.identity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    @Query("select distinct u from UserEntity u inner join u.roles r where r.name = :name")
    Optional<UserEntity> findUserByRoleName(@Param("name") String name);

    @Modifying
    @Query("update UserEntity u set u.userStatus = :status where u.keycloakId = :kid")
    int updateStatusByKeycloakId(@Param("status") UserStatus status, @Param("kid") String keycloakId);
}
