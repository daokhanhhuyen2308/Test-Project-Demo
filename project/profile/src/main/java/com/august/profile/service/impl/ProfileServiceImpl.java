package com.august.profile.service.impl;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.entity.UserProfile;
import com.august.protocol.file.FilePurpose;
import com.august.protocol.file.FileServiceGrpc;
import com.august.protocol.file.UploadFileRequest;
import com.august.protocol.file.UploadFileResponse;
import com.august.sharecore.events.ProfileCreatedEvent;
import com.august.sharecore.events.UserRegisteredEvent;
import com.august.profile.mapper.ProfileMapper;
import com.august.profile.repository.ProfileRepository;
import com.august.profile.service.ProfileService;
import com.august.protocol.profile.*;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.august.sharesecurity.dto.AuthCurrentUser;
import com.august.sharesecurity.utils.SecurityUtils;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    @GrpcClient("file-service-grpc")
    private FileServiceGrpc.FileServiceBlockingStub fileStub;
    private final SecurityUtils securityUtils;
    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Override
    public ApiResponse<ProfileResponse> getInfoUserProfile(String profileId) {
        UserProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppCustomException(ErrorCode.USER_NOT_FOUND));

        ApiResponse<ProfileResponse> response = new ApiResponse<>();
        response.setResult(profileMapper.mapToResponse(profile));
        return response;
    }

    @Override
    public ProfileResponse uploadFileAvatar(MultipartFile avatar) {
        try {
            AuthCurrentUser currentUser = securityUtils.getCurrentUser();

            RealmResource realmResource = keycloak.realm(realm);

            String keycloakId = currentUser.getKeycloakId();

            UploadFileRequest request = UploadFileRequest.newBuilder()
                    .setFile(ByteString.copyFrom(avatar.getBytes()))
                    .setFileName(avatar.getOriginalFilename())
                    .setContentType(avatar.getContentType())
                    .setOwnerId(keycloakId)
                    .setPurpose(FilePurpose.AVATAR)
                    .build();

            UploadFileResponse response = fileStub.uploadFile(request);

            String avatarUrl = response.getUrl();

            UserResource usersResource = realmResource.users().get(keycloakId);
            UserRepresentation userRepresentation = usersResource.toRepresentation();
            userRepresentation.singleAttribute("Avatar", avatarUrl);

            usersResource.update(userRepresentation);

            UserProfile profile = profileRepository.findByKeycloakId(keycloakId);
            profile.setAvatarUrl(avatarUrl);
            profileRepository.save(profile);

            return profileMapper.mapToResponse(profile);

        } catch (IOException e) {
            throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public ProfileResponse createProfile(CreateProfileRequest request) {
        UserProfile userProfile = profileMapper.mapToProfileEntity(request);
        UserProfile saveUserProfile = profileRepository.save(userProfile);
        return profileMapper.mapToResponse(saveUserProfile);
    }

    @Override
    public void createProfileForUserRegistered(UserRegisteredEvent event) {
        if (profileRepository.existsByKeycloakId(event.getKeycloakId())) {
            return;
        }

        UserProfile profile = UserProfile.builder()
                .email(event.getEmail())
                .username(event.getUsername())
                .keycloakId(event.getKeycloakId())
                .avatarUrl(event.getAvatarUrl())
                .build();

        profileRepository.save(profile);

        try{

            ProfileCreatedEvent profileCreatedEvent = new ProfileCreatedEvent(profile.getId(),
                    profile.getKeycloakId(), "PROFILE_SERVICE", Instant.now());
            kafkaTemplate.send("profile-created", profile.getKeycloakId(), profileCreatedEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null){
                            log.info("Published profile-created for keycloakId={}", profile.getKeycloakId());
                        }
                        else log.error("Publish profile-created failed for keycloakId={}, err={}",
                                profile.getKeycloakId(), ex.getMessage(), ex);
                    });

        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.CANNOT_SERIALIZE_EVENT);
        }

    }
}
