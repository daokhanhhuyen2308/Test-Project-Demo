package com.august.identity.service.impl;

import com.august.sharecore.constant.DefaultRoles;
import com.august.identity.dto.requests.UserCreateRequest;
import com.august.identity.dto.responses.UserResponse;
import com.august.identity.entity.OutboxEvent;
import com.august.identity.entity.RoleEntity;
import com.august.identity.entity.UserEntity;
import com.august.identity.enums.AggregateType;
import com.august.identity.enums.EventTopic;
import com.august.identity.enums.OutboxStatus;
import com.august.identity.enums.UserStatus;
import com.august.identity.events.OutboxNotificationEvent;
import com.august.sharecore.events.ProfileCreatedEvent;
import com.august.sharecore.events.UserRegisteredEvent;
import com.august.identity.mapper.UserMapper;
import com.august.identity.repository.OutboxEventRepository;
import com.august.identity.repository.RoleRepository;
import com.august.identity.repository.UserRepository;
import com.august.identity.service.UserService;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {

        RealmResource realmResource = keycloak.realm(realm);
        UserRepresentation kcUser = getUserRepresentation(request, realmResource);
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
            user.setRoles(roleEntities);
            user.setUserStatus(UserStatus.PENDING_PROFILE);
            userRepository.save(user);

            String commonId = UUID.randomUUID().toString();

            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .eventId(commonId)
                    .keycloakId(keycloakUserId)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .source("IDENTITY_SERVICE")
                    .createdAt(Instant.now())
                    .build();

            String payloadJson;

            try {
                payloadJson = objectMapper.writeValueAsString(event);
            } catch (Exception e) {
                throw new AppCustomException(ErrorCode.CANNOT_SERIALIZE_EVENT);
            }

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(commonId)
                    .aggregateType(AggregateType.USER)
                    .aggregateId(keycloakUserId)
                    .payload(payloadJson)
                    .outboxStatus(OutboxStatus.PENDING)
                    .topic(EventTopic.USER_REGISTERED)
                    .build();

            outboxEventRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxNotificationEvent(commonId));

            return userMapper.mapToResponse(user);
        }
    }

    @Transactional
    @Override
    public void consumerProfileCreated(ProfileCreatedEvent event) {
            String keycloakId = event.getKeycloakId();
            int updated = userRepository.updateStatusByKeycloakId(UserStatus.ACTIVE, keycloakId);
        if (updated == 0) {
            log.warn("ProfileCreated received but user not found. keycloakId={}", event.getKeycloakId());
            return;
        }

        log.info("User status updated to ACTIVE. keycloakId={}", event.getKeycloakId());
    }

    private UserRepresentation getUserRepresentation(UserCreateRequest request, RealmResource realmResource) {
        checkUsernameUserWithKC(request, realmResource);
        checkEmailUserWithKC(request, realmResource);

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

    private void checkUsernameUserWithKC(UserCreateRequest request, RealmResource realmResource){
        List<UserRepresentation> searchByUsername = realmResource.users()
                .searchByUsername(request.getUsername(), true);
        if (!searchByUsername.isEmpty()) {
            throw new AppCustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    public void checkEmailUserWithKC(UserCreateRequest request, RealmResource realmResource){
        List<UserRepresentation> searchByEmail = realmResource.users()
                .searchByEmail(request.getEmail(), true);

        if (!searchByEmail.isEmpty()) {
            throw new AppCustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
