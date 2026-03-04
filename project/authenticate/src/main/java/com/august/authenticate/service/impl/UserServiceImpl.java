package com.august.authenticate.service.impl;

import com.august.authenticate.constant.DefaultRoles;
import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;
import com.august.authenticate.entity.RoleEntity;
import com.august.authenticate.entity.UserEntity;
import com.august.authenticate.events.UserRegisteredEvent;
import com.august.authenticate.mapper.UserMapper;
import com.august.authenticate.repository.RoleRepository;
import com.august.authenticate.repository.UserRepository;
import com.august.authenticate.service.UserService;
import com.august.protocol.profile.CreateProfileRequest;
import com.august.protocol.profile.CreateProfileResponse;
import com.august.protocol.profile.ProfileServiceGrpc;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @GrpcClient("profile-service-grpc")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileServiceBlockingStub;

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        boolean existedEmail = userRepository.existsByEmail(request.getEmail());
        boolean existedUsername = userRepository.existsByUsername(request.getUsername());
        UserRepresentation kcUser = getUserRepresentation(request, existedEmail, existedUsername);

        RealmResource realmResource = keycloak.realm(realm);
        Response response = realmResource.users().create(kcUser);

        String keycloakUserId;

        try (response) {
            int status = response.getStatus();
            String body = null;

            try{
                body = response.readEntity(String.class);
            } catch (Exception e) {
                System.out.println();
            }
            if (status == 201) {
                keycloakUserId = CreatedResponseUtil.getCreatedId(response);
                RoleRepresentation userRole = realmResource.roles()
                        .get("USER")
                        .toRepresentation();

                realmResource.users()
                        .get(keycloakUserId)
                        .roles()
                        .realmLevel()
                        .add(Collections.singletonList(userRole));
            }
            else if (status == 409){
                String msg = (body == null) ? "" : body.toLowerCase();
                if (msg.contains("same email")) {
                    throw new AppCustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
                }
                if (msg.contains("same username")) {
                    throw new AppCustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
                }
                throw new AppCustomException(ErrorCode.USER_ALREADY_EXISTS);
            }
            else if (status == 401 || status == 403) {
                throw new AppCustomException(ErrorCode.KEYCLOAK_FORBIDDEN);
            }
            else throw new AppCustomException(ErrorCode.CAN_NOT_CONNECT_KEYCLOAK);

            Set<RoleEntity> roleEntities = new HashSet<>();

            roleRepository.findById(DefaultRoles.USER_ROLE).ifPresent(roleEntities::add);
            roleRepository.saveAll(roleEntities);

            UserEntity user = userMapper.mapToEntity(request);
            user.setKeycloakId(keycloakUserId);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(roleEntities);
            userRepository.save(user);

            CreateProfileRequest profileRequest = CreateProfileRequest.newBuilder()
                    .setKeycloakId(user.getKeycloakId())
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setAvatarUrl(user.getAvatarUrl())
                    .build();

            try {
                CreateProfileResponse profileResponse = profileServiceBlockingStub.createProfile(profileRequest);
                log.info("User profile id: {}", profileResponse.getProfileId());
                log.info("Message: {}", profileResponse.getMessage());

                UserRegisteredEvent event = UserRegisteredEvent.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .keycloakId(keycloakUserId)
                        .createdAt(LocalDateTime.now())
                        .source("AUTHENTICATE_SERVICE")
                        .build();

                kafkaTemplate.send("user-registration", event.getKeycloakId(), event);
                log.info("Published UserRegisteredEvent to Kafka for user: {}", event.getEmail());

            } catch (Exception e) {
                throw new AppCustomException(ErrorCode.DO_NOT_CONNECT_TO_GRPC);
            }

            log.info("saved user id={} username={} email={} keycloakId={} roles={}",
                    user.getId(), user.getUsername(), user.getEmail(), user.getKeycloakId(),
                    user.getRoles() == null ? 0 : user.getRoles().size()
            );

            return userMapper.mapToResponse(user);
        }
    }

    private static UserRepresentation getUserRepresentation(UserCreateRequest request,
                                                            boolean existedEmail,
                                                            boolean existedUsername) {
        if (Boolean.TRUE.equals(existedEmail)) {
            throw new AppCustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (Boolean.TRUE.equals(existedUsername)) throw new AppCustomException(ErrorCode.USERNAME_ALREADY_EXISTS);

        //Set password cho Keycloak
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getUsername());
        kcUser.setEmail(request.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setCredentials(Collections.singletonList(credential));
        kcUser.singleAttribute("Avatar", request.getAvatarUrl());
        return kcUser;
    }
}
