package com.example.authenticate.config;

import com.example.authenticate.constant.DefaultRoles;
import com.example.authenticate.entity.RoleEntity;
import com.example.authenticate.entity.UserEntity;
import com.example.authenticate.repository.RoleRepository;
import com.example.authenticate.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;

  @Value("${ADMIN.USER_NAME}")
  private String ADMIN_USER_NAME;

  @Value("${ADMIN.USER_PASSWORD}")
  private String ADMIN_USER_PASSWORD;

  public ApplicationInitConfig(
      UserRepository userRepository,
      @Lazy PasswordEncoder passwordEncoder,
      RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    log.info("Initializing ApplicationRunner...");

    if (userRepository.findUserByRoleName(DefaultRoles.ADMIN_ROLE).isEmpty()) {
      roleRepository.save(
          RoleEntity.builder().name(DefaultRoles.USER_ROLE).description("User role").build());

      var roles = new HashSet<RoleEntity>();

      roles.add(RoleEntity.builder().name(DefaultRoles.ADMIN_ROLE).description("Admin role").build());


      UserEntity user =
          UserEntity.builder()
              .username(ADMIN_USER_NAME)
              .email("admin@gmail.com")
              .password(passwordEncoder.encode(ADMIN_USER_PASSWORD))
              .roles(roles)
              .build();

      roleRepository.saveAll(roles);
      userRepository.save(user);
      log.error("Admin has been created with default. Please change it!");
    }
  }
}
