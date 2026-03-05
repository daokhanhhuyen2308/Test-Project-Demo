package com.august.identity.config;

import com.august.sharecore.constant.DefaultRoles;
import com.august.identity.entity.RoleEntity;
import com.august.identity.entity.UserEntity;
import com.august.identity.repository.RoleRepository;
import com.august.identity.repository.UserRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "spring",
    value = "datasource.driver-class-name",
    havingValue = "com.mysql.cj.jdbc.Driver")
public class ApplicationInitConfig implements ApplicationRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Value("${ADMIN.USER_NAME}")
  private String ADMIN_USER_NAME;

  public ApplicationInitConfig(
      UserRepository userRepository,
      RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public void run(@NonNull ApplicationArguments args) {
    log.info("Initializing ApplicationRunner...");

    if (userRepository.findUserByRoleName(DefaultRoles.ADMIN_ROLE).isEmpty()) {
      roleRepository.save(
          RoleEntity.builder().name(DefaultRoles.USER_ROLE).description("User role").build());

      var roles = new HashSet<RoleEntity>();

      roles.add(RoleEntity.builder().name(DefaultRoles.ADMIN_ROLE).description("Admin role").build());

      UserEntity user =
          UserEntity.builder()
              .username(ADMIN_USER_NAME)
              .email("adminadmin@gmail.com")
//              .keycloakId()
              .roles(roles)
              .build();

      roleRepository.saveAll(roles);
      userRepository.save(user);
      log.error("Admin has been created with default. Please change it!");
    }
  }
}
