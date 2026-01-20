package com.example.authenticate.repository;

import com.example.authenticate.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByEmail(String email);
    @Query("select distinct u from UserEntity u inner join u.roles r where r.name = :name")
    Optional<UserEntity> findUserByRoleName(@Param("name") String name);
}
